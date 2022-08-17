package com.anksystems.fenomy_pushk.service

import com.anksystems.fenomy_pushk.model.PushMessage
import org.springframework.stereotype.Service


@Service
class QueueToSend {
    val queue: LinkedHashMap<String, PushMessage> = LinkedHashMap()

}