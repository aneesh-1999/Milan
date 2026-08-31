package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.PreferencesManager
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class MilanSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MilanSyncWorker", "Executing background sync check for couple pings & updates...")

        try {
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }

            val prefs = PreferencesManager(appContext)
            if (!prefs.isLoggedIn) {
                return Result.success()
            }

            val currentPartnerId = prefs.currentPartnerId
            val otherPartnerId = if (currentPartnerId == Partner.MALTA.id) Partner.NEPAL.id else Partner.MALTA.id
            val firestore = FirebaseFirestore.getInstance()

            // 1. Check for new Thinking of You pings from partner
            val pingsSnapshot = firestore.collection("pings")
                .whereEqualTo("senderPartner", otherPartnerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (!pingsSnapshot.isEmpty) {
                val doc = pingsSnapshot.documents.first()
                val ping = doc.toObject(ThinkingOfYouPing::class.java)
                if (ping != null && ping.timestamp > prefs.lastNotifiedPingTime) {
                    Log.d("MilanSyncWorker", "Found new unnotified ping from ${ping.senderDisplayName}!")
                    prefs.lastNotifiedPingTime = ping.timestamp

                    NotificationHelper.showThinkingOfYouNotification(
                        context = appContext,
                        senderName = ping.senderDisplayName,
                        message = ping.message
                    )
                    NotificationHelper.playPingSound(appContext)
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.w("MilanSyncWorker", "Background sync worker check failed: ${e.message}")
            return Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "milan_couple_background_sync"

        fun schedulePeriodicSync(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<MilanSyncWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
                Log.d("MilanSyncWorker", "Periodic WorkManager background sync scheduled successfully.")
            } catch (e: Exception) {
                Log.w("MilanSyncWorker", "Could not schedule WorkManager: ${e.message}")
            }
        }
    }
}
