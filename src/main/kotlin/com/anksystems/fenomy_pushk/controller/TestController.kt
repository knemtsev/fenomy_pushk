package com.anksystems.fenomy_pushk.controller

import com.anksystems.fenomy_pushk.model.Note
import com.anksystems.fenomy_pushk.service.FirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessagingException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.time.ZonedDateTime


@Controller
class TestController {
    private val firebaseService: FirebaseMessagingService? by lazy { FirebaseMessagingService() }

    @RequestMapping("/send-notification")
    @ResponseBody
    @Throws(FirebaseMessagingException::class)
    fun sendNotification(
//        @RequestBody note: Note?,
//        @RequestParam topic: String?
    ): String? {
        val messages = mutableListOf<String>()

        sendNMessages(5)

        return "OK"
    }

    fun sendNMessages(num: Int)  {
        repeat(num) { count ->
            GlobalScope.launch {
                val note = Note("$count Test title $count", "$count Test message body $count")
                val token =
                    "e-dxmSZ0SCugo3t-y3_GL5:APA91bEBotZ1D7pTYqE1u9VMm5bcMGYO4Xsyy0Ia1vUx5HCkZCI1VsqXxbyJ5sxHQsa2lQ3P4QiQFRmwKdkqHkMCaEoZdKWlJH9d83_bN1WsMTIfUTQBW1FgG4aoEsoMzq7afirvjIdZ"
                val message = firebaseService!!.sendNotification(note, token)
                //messages.add(message ?: "<null>")
                val time = ZonedDateTime.now()
                println("Send message: $count $time $message")
            }
        }
    }

}