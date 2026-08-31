package com.example.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FcmNotificationSender {

    private const val FCM_SEND_URL = "https://fcm.googleapis.com/fcm/send"
    // Firebase Server / Web API Key for project milan-85395
    private const val FCM_SERVER_KEY = "AIzaSyDPowsVQ1ymO0Aa5TKL3bCD1vM9WylThTM"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun sendPushNotification(
        recipientToken: String,
        title: String,
        body: String,
        type: String = "ping",
        senderName: String = "Milan",
        channelId: String = NotificationHelper.CHANNEL_ID_PINGS
    ): Boolean = withContext(Dispatchers.IO) {
        if (recipientToken.isBlank()) {
            Log.w("FcmSender", "Recipient token is blank, skipping push dispatch")
            return@withContext false
        }

        try {
            val root = JSONObject()
            root.put("to", recipientToken)
            root.put("priority", "high")

            // System tray notification for Android when app is completely closed/killed
            val notification = JSONObject()
            notification.put("title", title)
            notification.put("body", body)
            notification.put("sound", "milan_ping")
            notification.put("android_channel_id", channelId)
            notification.put("click_action", "FLUTTER_NOTIFICATION_CLICK")
            root.put("notification", notification)

            // Custom payload data for onMessageReceived
            val data = JSONObject()
            data.put("type", type)
            data.put("sender_name", senderName)
            data.put("message", body)
            data.put("title", title)
            root.put("data", data)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(FCM_SEND_URL)
                .addHeader("Authorization", "key=$FCM_SERVER_KEY")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("FcmSender", "FCM push dispatched: code=${response.code}, body=$responseBody")
            return@withContext response.isSuccessful
        } catch (e: Exception) {
            Log.e("FcmSender", "Error dispatching FCM push notification: ${e.message}", e)
            return@withContext false
        }
    }
}
