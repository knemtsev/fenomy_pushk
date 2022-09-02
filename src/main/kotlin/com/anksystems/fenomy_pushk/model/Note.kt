package com.anksystems.fenomy_pushk.model

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidConfig.Priority

data class Note(
    val subject: String,
    val content: String? = null,
    val data: Map<String, String?>? = null,
    val image: String? = null,
    val priority: String = DEF_PRIORITY,
    val collapseKey: String? = null,
) {
    companion object {
        const val DEF_PRIORITY = "normal"
    }

    fun getPriority(): Priority =
        when(priority) {
            "normal" -> Priority.NORMAL
            "high" -> Priority.HIGH
            else -> Priority.NORMAL
        }


}