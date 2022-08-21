package com.anksystems.fenomy_pushk.db.dao

import org.jetbrains.exposed.sql.Table

object DeviceTable : Table("db.device") {
    val id = uuid("id")
    val document = uuid("document")
    val model = uuid("model")
    val client = uuid("client").nullable()
    val identity = text("identity")
    val version  = text("version").nullable()
    val serial  = text("serial").nullable()
    val address = text("address").nullable()
    val iccid   = text("iccid").nullable()
    val imsi    = text("imsi").nullable()
}
