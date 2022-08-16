package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.Properties
import com.anksystems.fenomy_pushk.model.PushMessage
import com.google.firebase.messaging.FirebaseMessagingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SendMessageService(
    @Autowired
    private val props: Properties,
    @Autowired
    private val fms: FirebaseMessagingService
) {
    val serviceScope = CoroutineScope(Dispatchers.IO)
    val outQueue = Channel<PushMessage>()
    val numWorkers = props.sendWorkers


    init {
        println("${props}")
    }
    fun initWorkers() {
        println(numWorkers)
    }


    fun send(message: PushMessage) {
        serviceScope.launch {
            try {
                val result = fms.sendNotification(message.note, message.token, message.topic)
                println("OK: $result")
            } catch (e: FirebaseMessagingException) {
                println("msg_error=${e.messagingErrorCode} error=${e.errorCode}\nmessage=[${message}]")
            }
        }
    }
}