package com.anksystems.fenomy_pushk.db.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object PushPackageTable : Table("db.push_package") {
    val id = uuid("id")
    val status = text("status")
    val cdate = datetime("cdate")
    val udate = datetime("udate")
}
