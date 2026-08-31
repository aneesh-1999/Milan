package com.example.service

import android.util.Log
import com.example.data.local.PreferencesManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MilanFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MilanFCM", "New FCM registration token received: $token")
        val prefs = PreferencesManager(applicationContext)
        prefs.fcmToken = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MilanFCM", "Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"] ?: "ping"
        val senderName = data["sender_name"] ?: remoteMessage.notification?.title ?: "Your Love"
        val message = data["message"] ?: remoteMessage.notification?.body ?: "Thinking of you right now"

        when (type) {
            "ping" -> {
                NotificationHelper.showThinkingOfYouNotification(
                    context = applicationContext,
                    senderName = senderName,
                    message = message
                )
            }
            "diary" -> {
                NotificationHelper.showDiaryNotification(
                    context = applicationContext,
                    senderName = senderName,
                    caption = message
                )
            }
            else -> {
                NotificationHelper.showThinkingOfYouNotification(
                    context = applicationContext,
                    senderName = senderName,
                    message = message
                )
            }
        }
    }
}
