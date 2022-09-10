package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.FenomyPushkApplication
import com.anksystems.fenomy_pushk.MyProperties
import com.anksystems.fenomy_pushk.db.dao.DeviceTable
import com.anksystems.fenomy_pushk.db.dao.PushTable
import com.anksystems.fenomy_pushk.ext.decodeFromStringSafe
import com.anksystems.fenomy_pushk.model.*
import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.api.jdbc.PGNotificationListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.sql.SQLException
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*


@Service
class PGListenerService(
    @Autowired private val props: MyProperties,
    @Autowired private val env: Environment,
    @Autowired private val sendMessageService: SendMessageService,
    @Autowired private val log: LogService,
    @Autowired private val app: FenomyPushkApplication
) {

    private val config by lazy {
        HikariConfig().apply {
            jdbcUrl = env.getProperty("spring.datasource.url") //props.dbUrl
            username = props.dbUser
            password = props.dbPassword
            driverClassName = env.getProperty("spring.datasource.driver-class-name")
            //keepaliveTime = 60000

        }
    }

    private val ds by lazy {
        HikariDataSource(config)
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val queueToUpdateStatus = sendMessageService.getQueueToUpdateStatus()

    private var lastNotificationTime: ZonedDateTime = ZonedDateTime.now()

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        coerceInputValues = true
    }

    init {
        initService()
    }

    private fun initService() {
        log.i("${this.javaClass.name} start ${app.getVersion()}")

        if (areThereNewPushes()) {
            serviceScope.launch {
                processNewPushes()
                initListener()
            }
        } else {
            initListener()
        }
        // обновление статуса пушей
        serviceScope.launch {
            while (true) {
                val listToUpdate = mutableListOf(queueToUpdateStatus.get())
                delay(100) // Если статусы идут потоком, ждём, когда накопятся
                var count = 1
                do {
                    val next = queueToUpdateStatus.getOrNull()
                    next?.let { listToUpdate.add(next) }
                    count += 1
                } while (next != null && count < props.statusUpdatePackageSize) // обновляем пакетами
                updateStatus(listToUpdate)
            }
        }

        // переиодический перезапуск слушателя
        serviceScope.launch {
            while (true) {
                delay(props.listenerTestRestartPeriod)
                log.d("STATS: ${sendMessageService.getStats()}")
                val interval = ChronoUnit.MILLIS.between(lastNotificationTime, ZonedDateTime.now())
                if (interval > props.listenerTestRestartPeriod) {
                    if (areThereNewPushes()) {
                        log.d("TEST period: $props.listenerTestRestartPeriod < $interval ms")
                        log.e("RESTART. Are there new pushes, but listener did not process them. Restart listener.")
                        resetListener()
                        processNewPushes()
                        initListener()
                    }
                }
            }
        }
    }

    private fun areThereNewPushes() = countNewPushes() > 0
    private fun countNewPushes(): Long {
        var count = 0L
        try {
            Database.connect(ds)
            transaction {
                count = PushTable.select { PushTable.status.eq(PushStatus.NEW.status) }.count()
            }
        } catch (e: Exception) {
            log.e(e)
        }
        return count
    }

    private suspend fun processNewPushes() {
        try {
            lateinit var listPushes: List<PushMessage>
            Database.connect(ds)
            transaction {
                listPushes = PushTable.select { PushTable.status.eq(PushStatus.NEW.status) }.map { row ->
                    PushMessage(
                        id = row[PushTable.id].toString(),
                        address = row[PushTable.address],
                        note = Note(
                            subject = row[PushTable.subject] ?: "",
                            content = row[PushTable.content],
                            data = row[PushTable.data]?.let {
                                json.decodeFromStringSafe<Map<String, String?>>(
                                    it
                                )
                            },
                            image = row[PushTable.image],
                            priority = row[PushTable.priority],
                            collapseKey = row[PushTable.collapseKey]
                        )
                    )
                }
            }
            //.limit(PROCESS_NEW_PUSHES_PACKAGE_SIZE)
            listPushes.forEach { push ->
                sendMessageService.send(push)
            }
        } catch (e: SQLException) {
            log.e(e.message.toString())
        }
    }

    var pgConn: PGConnection? = null
    var notificationListener: PGNotificationListener? = null

    private fun resetListener() {
        pgConn?.let {
            try {
                it.removeNotificationListener(notificationListener)
                it.close()
            } catch (e: Exception) {
                log.e(e.message.toString())
            }
        }
    }

    private fun initListener() {
        log.d("${this.javaClass.name} listener start")

        notificationListener = object : PGNotificationListener {
            override fun notification(processId: Int, channelName: String, payload: String) {
                lastNotificationTime = ZonedDateTime.now()
                //println("Received from PG: $processId, $channelName")
                log.d("Received from PG: $processId, $channelName")
                log.t("Payload: $payload")
                try {
                    val notifyMessage = json.decodeFromStringSafe<NotifyMessage>(payload)
                    if (notifyMessage != null) {
                        log.t("notifyMessage: $notifyMessage")
                        val pushMessage = notifyMessage.toPushMessage()
                        log.d("Message ${pushMessage.note.collapseKey} id=${pushMessage.id} ")
                        log.t("pushMessage: $pushMessage")
                        serviceScope.launch {
                            sendMessageService.send(pushMessage)
                        }
                    } else {
                        log.e("Decode json error: '$payload'")
                    }
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            override fun closed() {
                log.e("Connection to Postgres lost! Try to reconnect...")
                serviceScope.launch {
                    delay(1000)
                    initListener()
                }
            }

        }

        try {
            val conn = ds.connection
            val pgConn = conn.unwrap(PGConnection::class.java)

            pgConn.addNotificationListener(notificationListener)
            pgConn.createStatement().use { statement -> statement.execute("LISTEN push;") }

        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun updateStatus(messageStatusList: List<PushMessageStatus>) {
        messageStatusList.filter { it.status == PushStatus.SUBMITTED }.takeIf { it.isNotEmpty() }
            ?.let { setStatus(it, PushStatus.SUBMITTED.status) }
        messageStatusList.filter { it.status == PushStatus.FAILED }.takeIf { it.isNotEmpty() }
            ?.let { list ->
                setStatus(list, PushStatus.FAILED.status)
                removeAddresses(list.map { it.address!! }.distinct())
            }
    }

    fun setStatus(messageStatusList: List<PushMessageStatus>, status: String) {
        try {
            Database.connect(ds)
            transaction {
                PushTable.update({ PushTable.id.inList(messageStatusList.map { UUID.fromString(it.id) }) }) {
                    it[PushTable.status] = status
                }
            }
        } catch (e: SQLException) {
            log.e(e.message.toString())
        }
    }

    fun removeAddresses(addressList: List<String>) {
        try {
            Database.connect(ds)
            transaction {
                DeviceTable.update({ DeviceTable.address.inList(addressList) }) {
                    it[address] = null
                }
            }
        } catch (e: SQLException) {
            log.e(e.message.toString())
        }
    }


}