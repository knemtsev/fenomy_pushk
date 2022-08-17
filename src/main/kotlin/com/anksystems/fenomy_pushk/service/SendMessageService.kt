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
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Service
class SendMessageService(
    @Autowired private val props: Properties,
    @Autowired private val fms: FirebaseMessagingService,
    @Autowired private val log: LogService,
) {
    val serviceScope = CoroutineScope(Dispatchers.IO)
    val outQueue = Channel<PushMessage>()
    val numWorkers = props.sendWorkers


    init {
        log.i("${props}")
    }

    fun initWorkers() {
        log.i("numWorkers $numWorkers")
    }


    fun send(message: PushMessage) {
        serviceScope.launch {
            val startTime = ZonedDateTime.now()
            try {
                val result = fms.sendNotification(message)
                val interval = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                log.d("[$interval ms] OK: $result")
            } catch (e: FirebaseMessagingException) {
                val interval = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                log.e("[$interval ms] ERROR: ${e.messagingErrorCode}")
            }
        }
    }
}