package com.anksystems.fenomy_pushk.db

import com.anksystems.fenomy_pushk.Properties
import com.anksystems.fenomy_pushk.model.NotifyMessage
import com.anksystems.fenomy_pushk.service.LogService
import com.anksystems.fenomy_pushk.service.SendMessageService
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
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.stereotype.Service
import java.sql.SQLException


@Service
class PGService(
    @Autowired private val props: Properties,
    @Autowired private val env: Environment,
    @Autowired private val sendMessageService: SendMessageService,
    @Autowired private val log: LogService,
) {

    val config by lazy {
        HikariConfig().apply {
            jdbcUrl = env.getProperty("spring.datasource.url") //props.dbUrl
            username = props.dbUser
            password = props.dbPassword
            driverClassName = env.getProperty("spring.datasource.driver-class-name")
            keepaliveTime = 60000

        }
    }

    val ds by lazy {
        HikariDataSource(config)
    }

    val serviceScope = CoroutineScope(Dispatchers.IO)

    init {
        println("******** ${env.getProperty("spring.datasource.url")} ***********")
        //test()
        initListener()
    }

    fun test() {
        val conn = Database.connect(ds)
        println(conn)
        //val conn = ds.connection
/*
        transaction {
            PushPackageTable.selectAll().let {
                println("$it")
                it.forEach { resultRow ->
                    println(resultRow[PushPackageTable.id])
                }
            }
        }
*/
    }

    private fun initListener() {
        log.i("${this.javaClass.name} start")
        val notificationListener = object: PGNotificationListener {
            override fun notification(processId: Int, channelName: String, payload: String) {
                //println("Received from PG: $processId, $channelName")
                log.d("Received from PG: $processId, $channelName")
                try {
                    val notifyMessage = Json.decodeFromString<NotifyMessage>(payload)
                    //println("message=$notifyMessage")
                    val pushMessage = notifyMessage.toPushMessage()
                    //println("pushMessage=$pushMessage")
                    log.d("Message id=${pushMessage.id}")
                    sendMessageService.send(pushMessage)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            override fun closed() {
                log.e("Connection to Postgres lost! Try to reconnect...")
                serviceScope.launch {
                    delay(5000)
                    initListener()
                }
            }

        }
        try {
            val conn = ds.connection
            val pgConn = conn.unwrap(PGConnection::class.java)

//            val connection = DataSourceUtils.getConnection(ds).unwrap(PGConnection::class.java)
            //pgConn.notifications.
            pgConn.addNotificationListener(notificationListener)
            pgConn.createStatement().use { statement -> statement.execute("LISTEN push;") }
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

}