package com.anksystems.fenomy_pushk.model

import java.util.concurrent.atomic.DoubleAdder

data class PushMessage(
    val id: String,
    val address: String,
    val note: Note
) {
    companion object {
        const val MAX_TOPIC_LEN = 32
    }
    fun topic() = address.takeIf { address.length<MAX_TOPIC_LEN }
    fun token() = address.takeIf { address.length>MAX_TOPIC_LEN }
}
