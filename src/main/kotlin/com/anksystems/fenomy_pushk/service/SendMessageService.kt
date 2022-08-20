package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.Properties
import com.anksystems.fenomy_pushk.lib.ConcurrentQueue
import com.anksystems.fenomy_pushk.model.PushMessage
import com.anksystems.fenomy_pushk.model.PushMessageStatus
import com.anksystems.fenomy_pushk.model.PushStatus
import com.google.firebase.messaging.FirebaseMessagingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
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
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val queueToSend = ConcurrentQueue<PushMessage>(props.queueSendPushSize)
    private val queueToUpdateStatus = ConcurrentQueue<PushMessageStatus>(props.queueUpdateStatusSize)

    init {
        log.i("${props}")
        initService()
    }

    fun getQueueToUpdateStatus() = queueToUpdateStatus

    private fun initService() {
        serviceScope.launch {
            do {
                sendPush(queueToSend.get())
            } while (true)
        }

    }

    suspend fun send(message: PushMessage) {
        queueToSend.put(message)
    }

    private val pushSendingPool = Semaphore(props.poolSendPushSize, 0)
    private suspend fun sendPush(message: PushMessage) {
        pushSendingPool.acquire()
        serviceScope.launch {
            val startTime = ZonedDateTime.now()
            try {
                val result = fms.sendNotification(message)
                val interval = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                queueToUpdateStatus.put(PushMessageStatus(message.id, PushStatus.SUBMITTED))
                log.d("[$interval ms] OK: $result")
            } catch (e: FirebaseMessagingException) {
                queueToUpdateStatus.put(PushMessageStatus(message.id, PushStatus.FAILED))
                val interval = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                log.e("[$interval ms] ERROR: ${e.messagingErrorCode}")
            }
            pushSendingPool.release()
        }
    }
}