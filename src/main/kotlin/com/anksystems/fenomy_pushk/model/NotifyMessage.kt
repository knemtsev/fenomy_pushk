package com.anksystems.fenomy_pushk.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement

@Serializable
data class NotifyMessage(
    @SerialName("id")               val id: String,
    @SerialName("cdate")            val cdate: String,
    @SerialName("address")          val address: String,
    @SerialName("subject")          val subject: String,
    @SerialName("content")          val content: String? = null,
    @SerialName("data")             val data: JsonElement,
    @SerialName("image")            val image: String? = null,
    @SerialName("priority")         val priority: String = Note.DEF_PRIORITY,
    @SerialName("collapse_key")     val collapseKey: String? = null,
) {
    fun toPushMessage() = PushMessage(
        id = id,
        address = address,
        note = Note (
            subject = subject,
            content = content,
            data = Json.decodeFromString<Map<String, String?>>(data.toString()),
            image = image,
            collapseKey = collapseKey,
            priority = priority,
                )
    )
}
