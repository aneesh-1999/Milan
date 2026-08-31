package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID_PINGS = "milan_channel_pings_v2"
    const val CHANNEL_ID_DIARY = "milan_channel_diary_v2"
    const val CHANNEL_ID_NOTES = "milan_channel_notes_v2"

    fun getPingSoundUri(context: Context): Uri {
        return Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/milan_ping")
    }

    fun playPingSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.milan_ping)
            mediaPlayer?.setOnCompletionListener { mp ->
                try {
                    mp.release()
                } catch (e: Exception) {
                    Log.w("NotificationHelper", "MediaPlayer release error: ${e.message}")
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Could not play sound: ${e.message}")
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = getPingSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Channel for Thinking of You Pings with Custom Sound
            val pingsChannel = NotificationChannel(
                CHANNEL_ID_PINGS,
                "Thinking of You",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Instant love pings from your partner in Malta or Nepal"
                enableLights(true)
                lightColor = Color.parseColor("#8B5CF6")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250, 100, 400) // heartbeat rhythm
                setSound(soundUri, audioAttributes)
            }

            // Channel for Shared Diary & Memories
            val diaryChannel = NotificationChannel(
                CHANNEL_ID_DIARY,
                "Shared Diary & Memories",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New shared photos and captions"
                enableLights(true)
                lightColor = Color.parseColor("#EC4899")
            }

            // Channel for Locked Notes
            val notesChannel = NotificationChannel(
                CHANNEL_ID_NOTES,
                "Locked Love Notes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a surprise locked note becomes ready to open"
                enableLights(true)
                lightColor = Color.parseColor("#A855F7")
                setSound(soundUri, audioAttributes)
            }

            notificationManager.createNotificationChannels(listOf(pingsChannel, diaryChannel, notesChannel))
        }
    }

    fun showThinkingOfYouNotification(
        context: Context,
        senderName: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "home")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = getPingSoundUri(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PINGS)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("$senderName is thinking of you")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nConnected with love across the miles."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.parseColor("#8B5CF6"))
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 100, 250, 100, 400))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (e: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission not granted yet
        }
    }

    fun showDiaryNotification(
        context: Context,
        senderName: String,
        caption: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "diary")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DIARY)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("New Shared Memory")
            .setContentText("$senderName: $caption")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.parseColor("#EC4899"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1002, notification)
        } catch (e: SecurityException) {
            // Permission catch
        }
    }
}
