package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.DiaryEntity
import com.example.data.local.MilanDatabase
import com.example.data.local.PreferencesManager
import com.example.model.CountdownEvent
import com.example.model.DiaryEntry
import com.example.model.LockedNote
import com.example.model.MoodEntry
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import com.example.service.FcmNotificationSender
import com.example.service.MilanKeepAliveService
import com.example.service.MilanSyncWorker
import com.example.service.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MilanRepository(private val context: Context) {

    private val db = MilanDatabase.getDatabase(context)
    private val diaryDao = db.diaryDao()
    private val prefs = PreferencesManager(context)
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null
    private var storage: FirebaseStorage? = null

    // State Flows for UI
    private val _currentPartner = MutableStateFlow(Partner.fromId(prefs.currentPartnerId))
    val currentPartner: StateFlow<Partner> = _currentPartner.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _lastPing = MutableStateFlow<ThinkingOfYouPing?>(null)
    val lastPing: StateFlow<ThinkingOfYouPing?> = _lastPing.asStateFlow()

    private val _partnerMood = MutableStateFlow<MoodEntry?>(null)
    val partnerMood: StateFlow<MoodEntry?> = _partnerMood.asStateFlow()

    private val _myMood = MutableStateFlow<MoodEntry?>(null)
    val myMood: StateFlow<MoodEntry?> = _myMood.asStateFlow()

    private val _countdown = MutableStateFlow(CountdownEvent())
    val countdown: StateFlow<CountdownEvent> = _countdown.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries.asStateFlow()

    private val _lockedNotes = MutableStateFlow<List<LockedNote>>(emptyList())
    val lockedNotes: StateFlow<List<LockedNote>> = _lockedNotes.asStateFlow()

    private val _isFirebaseActive = MutableStateFlow(false)
    val isFirebaseActive: StateFlow<Boolean> = _isFirebaseActive.asStateFlow()

    private var pingListener: ListenerRegistration? = null
    private var moodListener: ListenerRegistration? = null
    private var countdownListener: ListenerRegistration? = null
    private var diaryListener: ListenerRegistration? = null
    private var notesListener: ListenerRegistration? = null

    init {
        initFirebase()
        loadInitialData()
        fetchFcmToken()
        MilanSyncWorker.schedulePeriodicSync(context)
        if (prefs.isLoggedIn && prefs.isKeepAliveEnabled) {
            MilanKeepAliveService.start(context)
        }
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firestore = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()
            storage = FirebaseStorage.getInstance()
            _isFirebaseActive.value = true
            Log.d("MilanRepo", "Firebase successfully initialized!")

            // Sign in anonymously to enable rules authentication
            auth?.let { firebaseAuth ->
                if (firebaseAuth.currentUser == null) {
                    firebaseAuth.signInAnonymously().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("MilanRepo", "Firebase anonymous auth successful: ${task.result?.user?.uid}")
                        } else {
                            Log.w("MilanRepo", "Firebase anonymous auth error: ${task.exception?.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MilanRepo", "Firebase initialization fallback: ${e.message}")
        }
    }

    private fun fetchFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    prefs.fcmToken = token
                    Log.d("MilanRepo", "FCM token retrieved: $token")
                    saveTokenToFirestore(token)
                }
            }
        } catch (e: Exception) {
            Log.w("MilanRepo", "Could not fetch FCM token: ${e.message}")
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val fs = firestore ?: return
        val partner = _currentPartner.value
        try {
            fs.collection("partner_tokens").document(partner.id)
                .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis(), "partner" to partner.id))
        } catch (e: Exception) {
            Log.w("MilanRepo", "Error saving token to firestore: ${e.message}")
        }
    }

    private fun loadInitialData() {
        // Sample baseline data for first launch
        val initialPing = ThinkingOfYouPing(
            id = "welcome_ping",
            senderPartner = _currentPartner.value.otherPartner.id,
            senderDisplayName = _currentPartner.value.otherPartner.displayName,
            message = "Welcome to Milan! Thinking of you always",
            timestamp = System.currentTimeMillis() - (15 * 60 * 1000)
        )
        _lastPing.value = initialPing

        val initialPartnerMood = MoodEntry(
            id = "partner_initial_mood",
            partnerId = _currentPartner.value.otherPartner.id,
            moodKey = "loved",
            moodLabel = "Loved",
            note = "Missing you so much from ${if (_currentPartner.value == Partner.MALTA) "Nepal" else "Malta"}!",
            timestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
        )
        _partnerMood.value = initialPartnerMood

        val initialMyMood = MoodEntry(
            id = "my_initial_mood",
            partnerId = _currentPartner.value.id,
            moodKey = "excited",
            moodLabel = "Excited",
            note = "Can't wait to see you!",
            timestamp = System.currentTimeMillis() - (60 * 60 * 1000)
        )
        _myMood.value = initialMyMood

        // Initial sample diary memories
        val defaultDiary = listOf(
            DiaryEntry(
                id = "memory_1",
                authorPartner = Partner.MALTA.id,
                authorName = "Anish",
                caption = "Sunset at the Grand Harbour in Valletta. Look at that golden Mediterranean glow!",
                imageUrl = "https://images.unsplash.com/photo-1518684079-3c830dcef090?w=800&q=80",
                locationName = "Valletta, Malta",
                timestamp = System.currentTimeMillis() - (24L * 60 * 60 * 1000),
                loveCount = 3,
                lovedBy = listOf(Partner.NEPAL.id)
            ),
            DiaryEntry(
                id = "memory_2",
                authorPartner = Partner.NEPAL.id,
                authorName = "Puri",
                caption = "Morning mountain breeze overlooking the Kathmandu valley. Sending warmth to Malta!",
                imageUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800&q=80",
                locationName = "Kathmandu, Nepal",
                timestamp = System.currentTimeMillis() - (48L * 60 * 60 * 1000),
                loveCount = 2,
                lovedBy = listOf(Partner.MALTA.id)
            )
        )
        _diaryEntries.value = defaultDiary

        // Initial sample locked note
        val defaultNotes = listOf(
            LockedNote(
                id = "note_1",
                senderPartner = _currentPartner.value.otherPartner.id,
                senderName = _currentPartner.value.otherPartner.displayName,
                recipientPartner = _currentPartner.value.id,
                title = "Open on our next video date",
                secretContent = "I wrote this just to tell you how proud I am of everything you are doing. Even with 6,800 km between us, my heart beats only for you. I love you so much!",
                unlockTimestamp = System.currentTimeMillis() + (18L * 60 * 60 * 1000),
                isRevealed = false,
                hint = "A little love letter locked with a key of patience"
            ),
            LockedNote(
                id = "note_2",
                senderPartner = _currentPartner.value.id,
                senderName = _currentPartner.value.displayName,
                recipientPartner = _currentPartner.value.otherPartner.id,
                title = "For when you miss me at night",
                secretContent = "Whenever the distance feels heavy, look up at the moon. It is the very same sky holding both of us together. You are my home.",
                unlockTimestamp = System.currentTimeMillis() - (1000L), // already unlocked
                isRevealed = true,
                hint = "Unlocked and read with all my heart"
            )
        )
        _lockedNotes.value = defaultNotes

        // Observe Room DB for cached diary entries
        repositoryScope.launch {
            diaryDao.getAllEntries().collect { cached ->
                if (cached.isNotEmpty()) {
                    _diaryEntries.value = cached.map { it.toDiaryEntry() }
                } else {
                    // Populate default to Room
                    diaryDao.insertEntries(defaultDiary.map { DiaryEntity.fromDiaryEntry(it) })
                }
            }
        }

        // Attach Firestore Listeners if available
        attachFirestoreListeners()
    }

    fun attachFirestoreListeners() {
        val fs = firestore ?: return

        try {
            // Listen to Pings collection
            pingListener?.remove()
            pingListener = fs.collection("pings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val doc = snapshot.documents.firstOrNull() ?: return@addSnapshotListener
                    val ping = doc.toObject(ThinkingOfYouPing::class.java)
                    if (ping != null) {
                        val previous = _lastPing.value
                        _lastPing.value = ping
                        if (ping.senderPartner != _currentPartner.value.id && (previous == null || previous.id != ping.id)) {
                            NotificationHelper.showThinkingOfYouNotification(
                                context = context,
                                senderName = ping.senderDisplayName,
                                message = ping.message
                            )
                            NotificationHelper.playPingSound(context)
                        }
                    }
                }

            // Listen to Moods collection
            moodListener?.remove()
            moodListener = fs.collection("moods")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    for (doc in snapshot.documents) {
                        val mood = doc.toObject(MoodEntry::class.java) ?: continue
                        if (mood.partnerId == _currentPartner.value.id) {
                            _myMood.value = mood
                        } else {
                            _partnerMood.value = mood
                        }
                    }
                }

            // Listen to Countdown
            countdownListener?.remove()
            countdownListener = fs.collection("countdowns").document("primary_countdown")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val count = snapshot.toObject(CountdownEvent::class.java)
                    if (count != null) {
                        _countdown.value = count
                    }
                }

            // Listen to Diary Entries
            diaryListener?.remove()
            diaryListener = fs.collection("diary_entries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val entries = snapshot.documents.mapNotNull { it.toObject(DiaryEntry::class.java) }
                    if (entries.isNotEmpty()) {
                        _diaryEntries.value = entries
                        repositoryScope.launch {
                            diaryDao.insertEntries(entries.map { DiaryEntity.fromDiaryEntry(it) })
                        }
                    }
                }

            // Listen to Locked Notes
            notesListener?.remove()
            notesListener = fs.collection("locked_notes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val notes = snapshot.documents.mapNotNull { it.toObject(LockedNote::class.java) }
                    if (notes.isNotEmpty()) {
                        _lockedNotes.value = notes
                    }
                }
        } catch (e: Exception) {
            Log.w("MilanRepo", "Could not attach Firestore listeners: ${e.message}")
        }
    }

    fun login(partner: Partner) {
        _currentPartner.value = partner
        prefs.currentPartnerId = partner.id
        prefs.isLoggedIn = true
        _isLoggedIn.value = true
        saveTokenToFirestore(prefs.fcmToken ?: "")
        attachFirestoreListeners()
        MilanSyncWorker.schedulePeriodicSync(context)
        if (prefs.isKeepAliveEnabled) {
            MilanKeepAliveService.start(context)
        }
    }

    fun logout() {
        prefs.isLoggedIn = false
        _isLoggedIn.value = false
        MilanKeepAliveService.stop(context)
    }

    suspend fun sendThinkingOfYouPing(customMessage: String? = null): Result<ThinkingOfYouPing> {
        val partner = _currentPartner.value
        val ping = ThinkingOfYouPing(
            id = UUID.randomUUID().toString(),
            senderPartner = partner.id,
            senderDisplayName = if (partner == Partner.MALTA) prefs.customMaltaName else prefs.customNepalName,
            message = customMessage ?: "Thinking of you right now",
            timestamp = System.currentTimeMillis()
        )

        _lastPing.value = ping
        NotificationHelper.playPingSound(context)

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("pings").document(ping.id).set(ping).await()
                
                // Dispatch high-priority FCM Push Notification to recipient device
                val otherPartnerId = partner.otherPartner.id
                val tokenDoc = fs.collection("partner_tokens").document(otherPartnerId).get().await()
                val recipientToken = tokenDoc.getString("token")
                if (!recipientToken.isNullOrBlank()) {
                    FcmNotificationSender.sendPushNotification(
                        recipientToken = recipientToken,
                        title = "${ping.senderDisplayName} is thinking of you",
                        body = ping.message,
                        type = "ping",
                        senderName = ping.senderDisplayName,
                        channelId = NotificationHelper.CHANNEL_ID_PINGS
                    )
                }
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore ping sync failed: ${e.message}")
            }
        }

        return Result.success(ping)
    }

    suspend fun updateMood(moodKey: String, label: String, note: String): Result<MoodEntry> {
        val partner = _currentPartner.value
        val mood = MoodEntry(
            id = partner.id,
            partnerId = partner.id,
            moodKey = moodKey,
            moodLabel = label,
            note = note,
            timestamp = System.currentTimeMillis()
        )

        _myMood.value = mood

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("moods").document(partner.id).set(mood).await()
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore mood update failed: ${e.message}")
            }
        }

        return Result.success(mood)
    }

    suspend fun updateCountdown(event: CountdownEvent): Result<CountdownEvent> {
        val updated = event.copy(
            updatedByPartner = _currentPartner.value.id,
            updatedAt = System.currentTimeMillis()
        )
        _countdown.value = updated

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("countdowns").document("primary_countdown").set(updated).await()
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore countdown update failed: ${e.message}")
            }
        }

        return Result.success(updated)
    }

    suspend fun addDiaryEntry(caption: String, imageUri: Uri?, locationOverride: String? = null): Result<DiaryEntry> {
        val partner = _currentPartner.value
        val id = UUID.randomUUID().toString()
        var imageUrl = imageUri?.toString() ?: "https://images.unsplash.com/photo-1529333166437-7750a6dd5a70?w=800&q=80"

        // If real Firebase Storage is available and URI is a local content URI
        val st = storage
        if (st != null && imageUri != null && imageUri.scheme == "content") {
            try {
                val ref = st.reference.child("diary_photos/$id.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firebase storage upload fallback: ${e.message}")
            }
        }

        val entry = DiaryEntry(
            id = id,
            authorPartner = partner.id,
            authorName = if (partner == Partner.MALTA) prefs.customMaltaName else prefs.customNepalName,
            caption = caption,
            imageUrl = imageUrl,
            locationName = locationOverride ?: partner.cityName + ", " + partner.countryName,
            timestamp = System.currentTimeMillis(),
            loveCount = 0,
            lovedBy = emptyList()
        )

        val updatedList = listOf(entry) + _diaryEntries.value
        _diaryEntries.value = updatedList

        repositoryScope.launch {
            diaryDao.insertEntry(DiaryEntity.fromDiaryEntry(entry))
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("diary_entries").document(id).set(entry).await()

                // Dispatch FCM Push Notification to recipient device
                val otherPartnerId = partner.otherPartner.id
                val tokenDoc = fs.collection("partner_tokens").document(otherPartnerId).get().await()
                val recipientToken = tokenDoc.getString("token")
                if (!recipientToken.isNullOrBlank()) {
                    FcmNotificationSender.sendPushNotification(
                        recipientToken = recipientToken,
                        title = "${entry.authorName} added a memory",
                        body = entry.caption,
                        type = "diary",
                        senderName = entry.authorName,
                        channelId = NotificationHelper.CHANNEL_ID_DIARY
                    )
                }
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore diary entry failed: ${e.message}")
            }
        }

        return Result.success(entry)
    }

    suspend fun toggleDiaryLove(entryId: String): Result<Unit> {
        val partner = _currentPartner.value
        val currentList = _diaryEntries.value
        val target = currentList.find { it.id == entryId } ?: return Result.failure(Exception("Entry not found"))

        val alreadyLoved = target.lovedBy.contains(partner.id)
        val newLovedBy = if (alreadyLoved) target.lovedBy - partner.id else target.lovedBy + partner.id
        val newLoveCount = newLovedBy.size

        val updated = target.copy(lovedBy = newLovedBy, loveCount = newLoveCount)
        _diaryEntries.value = currentList.map { if (it.id == entryId) updated else it }

        repositoryScope.launch {
            diaryDao.insertEntry(DiaryEntity.fromDiaryEntry(updated))
        }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("diary_entries").document(entryId).set(updated).await()
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore diary love toggle failed: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    suspend fun addLockedNote(
        title: String,
        secretContent: String,
        unlockTimestamp: Long,
        hint: String
    ): Result<LockedNote> {
        val partner = _currentPartner.value
        val id = UUID.randomUUID().toString()
        val note = LockedNote(
            id = id,
            senderPartner = partner.id,
            senderName = if (partner == Partner.MALTA) prefs.customMaltaName else prefs.customNepalName,
            recipientPartner = partner.otherPartner.id,
            title = title,
            secretContent = secretContent,
            unlockTimestamp = unlockTimestamp,
            isRevealed = false,
            createdAt = System.currentTimeMillis(),
            hint = hint
        )

        _lockedNotes.value = listOf(note) + _lockedNotes.value

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("locked_notes").document(id).set(note).await()

                // Dispatch FCM Push Notification to recipient device
                val otherPartnerId = partner.otherPartner.id
                val tokenDoc = fs.collection("partner_tokens").document(otherPartnerId).get().await()
                val recipientToken = tokenDoc.getString("token")
                if (!recipientToken.isNullOrBlank()) {
                    FcmNotificationSender.sendPushNotification(
                        recipientToken = recipientToken,
                        title = "New Locked Note from ${note.senderName}",
                        body = "A surprise love note was sent! Unlocks soon: \"${note.title}\"",
                        type = "note",
                        senderName = note.senderName,
                        channelId = NotificationHelper.CHANNEL_ID_NOTES
                    )
                }
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore locked note add failed: ${e.message}")
            }
        }

        return Result.success(note)
    }

    suspend fun revealLockedNote(noteId: String): Result<Unit> {
        val currentList = _lockedNotes.value
        val target = currentList.find { it.id == noteId } ?: return Result.failure(Exception("Note not found"))

        val updated = target.copy(isRevealed = true)
        _lockedNotes.value = currentList.map { if (it.id == noteId) updated else it }

        val fs = firestore
        if (fs != null) {
            try {
                fs.collection("locked_notes").document(noteId).update("isRevealed", true).await()
            } catch (e: Exception) {
                Log.w("MilanRepo", "Firestore note reveal failed: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    fun updatePartnerNames(maltaName: String, nepalName: String) {
        prefs.customMaltaName = maltaName
        prefs.customNepalName = nepalName
    }

    fun getPartnerNames(): Pair<String, String> {
        return Pair(prefs.customMaltaName, prefs.customNepalName)
    }
}
