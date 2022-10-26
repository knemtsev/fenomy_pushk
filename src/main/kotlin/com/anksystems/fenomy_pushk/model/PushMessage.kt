package com.anksystems.fenomy_pushk.model

import com.anksystems.lib.Searchable

data class PushMessage(
    val id: String,
    val address: String,
    val note: Note
) : Searchable<PushMessage> {
    companion object {
        const val MAX_TOPIC_LEN = 32
    }
    fun topic() = address.takeIf { address.length<MAX_TOPIC_LEN }
    fun token() = address.takeIf { address.length>MAX_TOPIC_LEN }
    override fun compare(other: PushMessage): Boolean = other.id==this.id
}
