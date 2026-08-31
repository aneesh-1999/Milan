package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Milan", appName)
    }

    @Test
    fun `verify partner switching logic`() {
        val malta = Partner.MALTA
        val nepal = Partner.NEPAL

        assertEquals("malta_partner", malta.id)
        assertEquals("nepal_partner", nepal.id)
        assertEquals(nepal, malta.otherPartner)
        assertEquals(malta, nepal.otherPartner)
        assertEquals("Anish", malta.displayName)
        assertEquals("Puri", nepal.displayName)
    }

    @Test
    fun `verify ping construction`() {
        val ping = ThinkingOfYouPing(
            id = "test_ping_1",
            senderPartner = Partner.MALTA.id,
            senderDisplayName = Partner.MALTA.displayName,
            message = "Thinking of you 💜",
            timestamp = 1700000000000L
        )
        assertEquals("malta_partner", ping.senderPartner)
        assertEquals("Anish", ping.senderDisplayName)
        assertEquals(1700000000000L, ping.timestamp)
    }
}
