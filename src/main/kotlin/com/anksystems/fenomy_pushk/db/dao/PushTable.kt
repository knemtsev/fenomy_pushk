package com.anksystems.fenomy_pushk.db.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object PushTable : Table("db.push") {
    val id = uuid("id")
    val status = text("status")
    val cdate = datetime("cdate")
    val udate = datetime("udate")
    val address = text("address")
    val subject = text("subject").nullable()
    val content = text("content").nullable()
    val data = text("data").nullable()
    val image = text("image").nullable()
    val priority = text("priority")
    val collapseKey = text("collapse_key")
}
