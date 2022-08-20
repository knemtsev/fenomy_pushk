package com.anksystems.fenomy_pushk

import com.anksystems.fenomy_pushk.model.Note
import com.anksystems.fenomy_pushk.model.PushMessage
import com.anksystems.fenomy_pushk.service.SendMessageService
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
class FenomyPushkApplicationTests(
    @MockBean
    private val sms: SendMessageService
) {
/*

    @Test
    suspend fun testCoroutines() {
        sms.send(PushMessage(
            id="9381280742839742",
            address = "som329849023849028309482309854302574234987598023458923045908273495798243",
            note = Note (
                subject="test subject",
                content="test content",
                priority = Note.DEF_PRIORITY,
                collapseKey = Note.DEF_COLLAPSE_KEY,
                    )

        ))
    }
*/

}
