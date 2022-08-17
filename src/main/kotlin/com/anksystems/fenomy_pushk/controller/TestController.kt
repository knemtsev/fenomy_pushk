package com.anksystems.fenomy_pushk.controller

import com.anksystems.fenomy_pushk.model.Note
import com.anksystems.fenomy_pushk.model.PushMessage
import com.anksystems.fenomy_pushk.service.FirebaseMessagingService
import com.anksystems.fenomy_pushk.service.SendMessageService
import com.google.firebase.messaging.FirebaseMessagingException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.time.ZonedDateTime
import java.util.UUID


@Controller
class TestController(
    @Autowired private val sendMessagingService: SendMessageService,
    @Autowired private val firebaseService: FirebaseMessagingService
) {

    @RequestMapping("/send-notification")
    @ResponseBody
    @Throws(FirebaseMessagingException::class)
    fun sendNotification(
//        @RequestBody note: Note?,
//        @RequestParam topic: String?
    ): String? {

        val messages = mutableListOf<String>()
        adrs.forEach { address ->
            val newId=UUID.randomUUID().toString()
            messages.add(newId)
            sendMessagingService.send(
                PushMessage(
                    id = newId,
                    address = address,
                    note = Note(
                        subject = "Test messsage",
                        content = address.hashCode().toString()
                    )
            )
            )
        }

        return messages.joinToString("</br>\n") { it }
    }

    fun sendNMessages(num: Int)  {
        repeat(num) { count ->
            GlobalScope.launch {
                val note = Note("$count Test title $count", "$count Test message body $count")
                val token =
                    "e-dxmSZ0SCugo3t-y3_GL5:APA91bEBotZ1D7pTYqE1u9VMm5bcMGYO4Xsyy0Ia1vUx5HCkZCI1VsqXxbyJ5sxHQsa2lQ3P4QiQFRmwKdkqHkMCaEoZdKWlJH9d83_bN1WsMTIfUTQBW1FgG4aoEsoMzq7afirvjIdZ"

                val message = firebaseService!!.sendNotification(
                    PushMessage(
                    id = UUID.randomUUID().toString(),
                    address = token,
                    note = note
                )
                )
                //messages.add(message ?: "<null>")
                val time = ZonedDateTime.now()
                println("Send message: $count $time $message")
            }
        }
    }

    val adrs=listOf("eKhUJxEWRRaJsbjyGdfgyk:APA91bEMzYQy8cNvgChznEzmJJHVcMeKaXmaHjkxsLqlOGPj7w9GnGVIx80bbErm76Pn6I_VsJuAdK4P14rtgb7TWV9ihjjOn0o-aSGLx4SffGlUR5j529Mn5HwtsJIJNYTyc-1LaaJp",
    "e7APXE0tRxSRMYbwiOg2OZ:APA91bHEw0h2Ms-iLkACeLNjRKSpRp9Q2uVGpg3awRJhJ2czh2vkQDlAVA6VkKAzmONxVd3c6irEnv5HVGwezee65uQcneyXTuR88YfwaH2NelVWt96kz5PJiFaoqvYWPprQr3w9P4WW",
    "clFeL4pISGqbU7ODd86WZ5:APA91bGUvU1wvr5YYb-kHSycX0Qijl8MC8Gmb3OREWIgiKpFK2U1e6XTsvDfepOaJDgNxTq6lXePVG__btjz5QJ7J1TvxLqWTc_EYfuIKJ2lIlU54d0X5Qqtwa9nqMg-c5EeaM4JfMm7",
    "doMfNroeTP-64t_80M2-DC:APA91bGuagPFvEyJp8p0UHT8I38iCvpdsSzVGwiydKY9eSP7dIKB1861AZzhZ7_7AOJJrysejeF0GhCrAdzLbzdY-NNskWp6_HpsWGQ1Ywn8hZuzmtwjLI7aqQTpMpPNiJyAZm4LidDQ",
    "fRy6xc0DTj-5NDC41YkkPU:APA91bGRKCxkRkmrOT-7luN8D5eYleId4Tc2RjcODAq48r_gbidEihaQzExfTVmJEJTu7vXbTyzelv8mzUUqXE5XTjhufzDFdK6n90Yk0WSYIR-io3yBFkhgge-7pcJhe0eDBnP1kg9G",
    "frRbtPRjRIyTAEYJPDgSXk:APA91bGts_804zkOzhYjG8sX7gxOWN1TrWhVP9fHduW-NqczRW7UuMTu9eZgdTECHzcWkaSEZ3lFaROlJc7OIxQYypOOTePJLBXwsbP-sOWPQc9plwGggiWX2kixMGHL8GddC3r45nY3",
    "diOP3MbgS3urL4zJ64FQV7:APA91bHunDAS7o5a25HP5FnzZekIOtrrcwasbGPiqjXslgu325KpTgCP5PrivqbJJHGeh5TKnQqhPB3wIUjoxFTWc9hYlx1V8WIe65UQrKYWUi0Og0PiowSwPnfnl8nMpYa5gIY8rD6f",
    "fRmayAu2Rh2oXrRjKiD0Vu:APA91bFMyo7s6_HHi0q7aNHQdjbgsjThDxfPRHFmVjPlw4jgfVsxWNJ2ojOUNA_-3hpDRbaWnEeTQ1VZ4pil29nmjrlMjkmab050fWE7y2EL4a-B2TTwsjYfswYWpGOfAJk9zb47F3in",
    "dTbTyUKNQBG48tYnbtOvNq:APA91bFHQs8Bj86EaE92I6ePYnM_JcOx_j2AvvAnT5NDV4ElFQiOmynjBf-LqeJAWfr1jqpnCp41rs3q1crAVUj5p2xjZFLUQA9E46Ka-YPoNRbyKBWC76xiqhZEuruX1w6m8P6UoS0V",
    "e-dxmSZ0SCugo3t-y3_GL5:APA91bEBotZ1D7pTYqE1u9VMm5bcMGYO4Xsyy0Ia1vUx5HCkZCI1VsqXxbyJ5sxHQsa2lQ3P4QiQFRmwKdkqHkMCaEoZdKWlJH9d83_bN1WsMTIfUTQBW1FgG4aoEsoMzq7afirvjIdZ")

}