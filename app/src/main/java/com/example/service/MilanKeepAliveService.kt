package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.PreferencesManager
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MilanKeepAliveService : Service() {

    private var pingListener: ListenerRegistration? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("MilanKeepAlive", "Foreground keep-alive service started for real-time couple connection")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = PreferencesManager(applicationContext)
        if (!prefs.isLoggedIn || !prefs.isKeepAliveEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        createServiceNotificationChannel()
        val notification = buildForegroundNotification(prefs)
        startForeground(NOTIFICATION_ID, notification)

        startRealtimePingListener(prefs)

        return START_STICKY
    }

    private fun startRealtimePingListener(prefs: PreferencesManager) {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            val firestore = FirebaseFirestore.getInstance()
            val currentPartnerId = prefs.currentPartnerId
            val otherPartnerId = if (currentPartnerId == Partner.MALTA.id) Partner.NEPAL.id else Partner.MALTA.id

            pingListener?.remove()
            pingListener = firestore.collection("pings")
                .whereEqualTo("senderPartner", otherPartnerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || snapshot.isEmpty) return@addSnapshotListener
                    val doc = snapshot.documents.firstOrNull() ?: return@addSnapshotListener
                    val ping = doc.toObject(ThinkingOfYouPing::class.java) ?: return@addSnapshotListener

                    if (ping.timestamp > prefs.lastNotifiedPingTime) {
                        prefs.lastNotifiedPingTime = ping.timestamp
                        NotificationHelper.showThinkingOfYouNotification(
                            context = applicationContext,
                            senderName = ping.senderDisplayName,
                            message = ping.message
                        )
                        NotificationHelper.playPingSound(applicationContext)
                    }
                }
        } catch (e: Exception) {
            Log.w("MilanKeepAlive", "Error attaching realtime listener: ${e.message}")
        }
    }

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Milan Background Connection",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps real-time alert sync active when app is closed"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(prefs: PreferencesManager): Notification {
        val partnerName = if (prefs.currentPartnerId == Partner.MALTA.id) prefs.customNepalName else prefs.customMaltaName
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Milan • Connected")
            .setContentText("Connected with $partnerName across the distance")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        pingListener?.remove()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "milan_keep_alive_channel"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, MilanKeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, MilanKeepAliveService::class.java)
            context.stopService(intent)
        }
    }
}
