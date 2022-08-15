package com.anksystems.fenomy_pushk.pojo

data class Note (
    val subject: String,
    val content: String? = null,
    val data: Map<String, String>? = null,
    val image: String? = null,
)