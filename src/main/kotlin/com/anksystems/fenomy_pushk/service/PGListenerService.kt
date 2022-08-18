package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.Properties
import com.anksystems.fenomy_pushk.db.dao.PushTable
import com.anksystems.fenomy_pushk.model.*
import com.impossibl.postgres.api.jdbc.PGConnection
import com.impossibl.postgres.api.jdbc.PGNotificationListener
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
    @Autowired private val props: Properties,
    @Autowired private val env: Environment,
    @Autowired private val sendMessageService: SendMessageService,
    @Autowired private val log: LogService,
) {

    companion object {
        const val RESTART_LISTENER_PERIOD = 60000L // в миллисекундах
    }

    val config by lazy {
        HikariConfig().apply {
            jdbcUrl = env.getProperty("spring.datasource.url") //props.dbUrl
            username = props.dbUser
            password = props.dbPassword
            driverClassName = env.getProperty("spring.datasource.driver-class-name")
            //keepaliveTime = 60000

        }
    }

    val ds by lazy {
        HikariDataSource(config)
    }

    val serviceScope = CoroutineScope(Dispatchers.IO)

    val queueToUpdateStatus = sendMessageService.getQueueToUpdateStatus()

    var lastNotificationTime: ZonedDateTime = ZonedDateTime.now()

    init {
        println("******** ${env.getProperty("spring.datasource.url")} ***********")
        //test()
        initService()
    }

    private fun initService() {
        log.i("${this.javaClass.name} start")
        println("${this.javaClass.name} start")

        initListener()

        // обновление статуса пушей
        serviceScope.launch {
            while (true) {
                val listToUpdate = mutableListOf(queueToUpdateStatus.get())
                delay(100)
                do {
                    val next = queueToUpdateStatus.getOrNull()
                    next?.let { listToUpdate.add(next) }
                } while (next != null)
                updateStatus(listToUpdate)
            }

        }

        // переиодический перезапуск слушателя
        serviceScope.launch {
            delay(RESTART_LISTENER_PERIOD)
            val interval = ChronoUnit.MILLIS.between(lastNotificationTime, ZonedDateTime.now())
            log.d("INTERVAL $interval ms")
            if(interval> RESTART_LISTENER_PERIOD) {
                resetListener()
                getNewPushes()
                initListener()
            }
        }
    }

    private fun getNewPushes() {
        try {
            Database.connect(ds)
            transaction {
                PushTable.select { PushTable.status.eq(PushStatus.NEW.status) }
                    .forEach {
                        sendMessageService.sendPush(
                            PushMessage(
                                id = it[PushTable.id].toString(),
                                address = it[PushTable.address],
                                note = Note(
                                    subject = it[PushTable.subject] ?: "",
                                    content = it[PushTable.content],
                                    data = it[PushTable.data]?.let { data -> Json.decodeFromString<Map<String,String>>(data)},
                                    image = it[PushTable.image],
                                    priority = it[PushTable.priority],
                                    collapseKey = it[PushTable.collapseKey]
                                )
                            )
                        )
                }
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

        notificationListener = object: PGNotificationListener {
            override fun notification(processId: Int, channelName: String, payload: String) {
                lastNotificationTime = ZonedDateTime.now()
                //println("Received from PG: $processId, $channelName")
                log.i("Received from PG: $processId, $channelName")
                log.t("Payload: $payload")
                try {
                    val notifyMessage = Json.decodeFromString<NotifyMessage>(payload)
                    log.t("notifyMessage: $notifyMessage")
                    val pushMessage = notifyMessage.toPushMessage()
                    log.d("Message id=${pushMessage.id}")
                    log.t("pushMessage: $pushMessage")
                    serviceScope.launch {
                        sendMessageService.send(pushMessage)
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
        messageStatusList.filter { it.status==PushStatus.SUBMITTED }.takeIf { it.isNotEmpty() }
            ?.let { setStatus(it,PushStatus.SUBMITTED.status) }
        messageStatusList.filter { it.status==PushStatus.FAILED }.takeIf { it.isNotEmpty() }
            ?.let { setStatus(it,PushStatus.FAILED.status) }
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

}