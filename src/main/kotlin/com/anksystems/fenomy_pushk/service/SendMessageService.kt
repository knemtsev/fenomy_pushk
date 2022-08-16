package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.model.PushMessage
import kotlinx.coroutines.channels.Channel
import org.springframework.stereotype.Service

@Service
class SendMessageService {
    val outQueue = Channel<PushMessage>()
    val numWorkers = 100
    val firebaseMessagingService by lazy { FirebaseMessagingService() }

    fun initWorkers(numWorkers: Int) {

    }
}