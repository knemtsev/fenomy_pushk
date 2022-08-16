package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.firebaseMessaging
import com.anksystems.fenomy_pushk.model.Note
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service


@Service
class FirebaseMessagingService {
    private val firebaseMessaging by lazy { firebaseMessaging() }

    @Throws(FirebaseMessagingException::class)
    fun sendNotification(note: Note?, token: String? = null, topic: String? = null): String? {

        if(note==null || (token==null && topic==null)) return null

        val notification: Notification = Notification
            .builder()
            .setTitle(note.subject)
            .setBody(note.content ?: note.subject)
            .apply {
                note.image?.let { setImage(it)}
            }

            .build()

        val message: Message = Message
            .builder().apply {
                token?.let { setToken(it) } ?: setTopic(topic)
            }
            .setNotification(notification)
            .apply { note.data?.let { putAllData(it) }  }
            .build()
        return firebaseMessaging!!.send(message)
    }

}