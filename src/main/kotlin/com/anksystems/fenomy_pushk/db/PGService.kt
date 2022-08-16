package com.anksystems.fenomy_pushk.db

import com.anksystems.fenomy_pushk.Properties
import com.anksystems.fenomy_pushk.db.dto.PushPackageTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.SpringProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service


@Service
class PGService(
    @Autowired private val props: Properties,
    @Autowired private val env: Environment,
) {

    val config by lazy {
        HikariConfig().apply {
            jdbcUrl = env.getProperty("spring.datasource.url") //props.dbUrl
            username = props.dbUser
            password = props.dbPassword
            driverClassName = props.dbDriver
            //keepaliveTime = 60000

        }
    }

    val ds by lazy {
        HikariDataSource(config)
    }

    init {
        println("******** ${env.getProperty("spring.datasource.url")} ***********")
        test()
    }

    fun test() {
        val conn = Database.connect(ds)
        println(conn)
        //val conn = ds.connection
        transaction {
            PushPackageTable.selectAll().let {
                println("$it")
                it.forEach { resultRow ->
                    println(resultRow[PushPackageTable.id])
                }

            }
        }
    }
}