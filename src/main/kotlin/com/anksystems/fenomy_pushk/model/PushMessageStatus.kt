package com.anksystems.fenomy_pushk.model

import com.anksystems.fenomy_pushk.lib.Searchable

data class PushMessageStatus(
    val id: String,
    val status: PushStatus = PushStatus.NEW,
    val address: String? = null
) : Searchable<PushMessageStatus> {

    override fun compare(other: PushMessageStatus): Boolean = other.id==this.id
}
