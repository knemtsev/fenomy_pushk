package com.anksystems.fenomy_pushk.model

data class PushMessage(
    val id: String,

    val token: String? = null,
    val topic: String? = null,
    val note: Note
)
