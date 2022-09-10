package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.firebaseMessaging
import com.anksystems.fenomy_pushk.model.PushMessage
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class FirebaseMessagingService(
    @Autowired private val log: LogService,
) {
    private val firebaseMessaging by lazy { firebaseMessaging() }

    @Throws(FirebaseMessagingException::class)
    fun sendNotification(pushMessage: PushMessage): String? {

        val token = pushMessage.token()
        val topic = pushMessage.topic()
        //println("token = $token  topic = $topic")

        if (token == null && topic == null) return null

        val notification: Notification = Notification
            .builder()
            //.setTitle(pushMessage.note.subject)
            .setBody(pushMessage.note.content ?: pushMessage.note.subject)
            .apply {
                pushMessage.note.image?.let { setImage(it) }
            }
            .build()

        log.d("collapse key=${pushMessage.note.collapseKey}")

        val androidConfig =
            if(pushMessage.note.collapseKey!=null && pushMessage.note.collapseKey!="notification")
                AndroidConfig.builder()
                    .setCollapseKey(pushMessage.note.collapseKey)
                    .setPriority(pushMessage.note.getPriority())
                    .build()
            else
                AndroidConfig.builder()
                    .setPriority(pushMessage.note.getPriority())
                    .build()

        val message: Message = Message
            .builder().apply {
                token?.let { setToken(it) } ?: setTopic(topic)
            }
            .setNotification(notification)
            .setAndroidConfig(androidConfig)
            .apply {
                pushMessage.note.data?.let {
                    putAllData(it.map { it.key to (it.value ?: "null") }.toMap())
                }
            }
            .build()

        val result = firebaseMessaging!!.send(message)
        return result
    }

}