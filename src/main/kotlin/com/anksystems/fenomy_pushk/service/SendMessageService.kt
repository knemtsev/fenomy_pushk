package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.MyProperties
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
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Service
class SendMessageService(
    @Autowired private val props: MyProperties,
    @Autowired private val fms: FirebaseMessagingService,
    @Autowired private val log: LogService,
) {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val queueToSend = ConcurrentQueue<PushMessage>(props.queueSendPushSize)
    private val queueToUpdateStatus = ConcurrentQueue<PushMessageStatus>(props.queueUpdateStatusSize)

    // statistic
    private var sentCount = AtomicInteger(0)
    private var sentCountSuccess = AtomicInteger(0)
    private var sentCountFailed = AtomicInteger(0)
    private var sentTotalTimeMs = AtomicLong(0L)

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
                sentCount.incrementAndGet()
                sentCountSuccess.incrementAndGet()
                sentTotalTimeMs.addAndGet(interval)
            } catch (e: FirebaseMessagingException) {
                queueToUpdateStatus.put(PushMessageStatus(message.id, PushStatus.FAILED, address = message.address))
                val interval = ChronoUnit.MILLIS.between(startTime, ZonedDateTime.now())
                log.e("[$interval ms] ERROR: ${e.messagingErrorCode}")
                sentCount.incrementAndGet()
                sentCountFailed.incrementAndGet()
                sentTotalTimeMs.addAndGet(interval)
            }
            pushSendingPool.release()
        }
    }

    fun getStats(): String {
        return "Total: ${sentCount.get()} +${sentCountSuccess.get()} -${sentCountFailed.get()} avg: ${sentTotalTimeMs.get()/(sentCount.get().takeIf { it!=0 } ?: 1)}"
    }
}