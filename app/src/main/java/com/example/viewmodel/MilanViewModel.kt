package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MilanRepository
import com.example.model.CountdownEvent
import com.example.model.DiaryEntry
import com.example.model.LockedNote
import com.example.model.MoodEntry
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class ClockState(
    val timeFormatted: String = "12:00:00 PM",
    val dateFormatted: String = "Monday, Aug 31",
    val hourOfDay: Int = 12,
    val isDaytime: Boolean = true,
    val zoneOffsetFormatted: String = "GMT+2"
)

data class CountdownRemaining(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isPassed: Boolean = false,
    val totalSeconds: Long = 0
)

class MilanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MilanRepository(application.applicationContext)

    // Current logged in partner
    val currentPartner: StateFlow<Partner> = repository.currentPartner
    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val isFirebaseActive: StateFlow<Boolean> = repository.isFirebaseActive

    // Core data streams
    val lastPing: StateFlow<ThinkingOfYouPing?> = repository.lastPing
    val partnerMood: StateFlow<MoodEntry?> = repository.partnerMood
    val myMood: StateFlow<MoodEntry?> = repository.myMood
    val countdown: StateFlow<CountdownEvent> = repository.countdown
    val diaryEntries: StateFlow<List<DiaryEntry>> = repository.diaryEntries
    val lockedNotes: StateFlow<List<LockedNote>> = repository.lockedNotes

    // Live Clocks
    private val _maltaClock = MutableStateFlow(ClockState())
    val maltaClock: StateFlow<ClockState> = _maltaClock.asStateFlow()

    private val _nepalClock = MutableStateFlow(ClockState())
    val nepalClock: StateFlow<ClockState> = _nepalClock.asStateFlow()

    private val _timeDifferenceText = MutableStateFlow("Nepal is 3 hrs 45 mins ahead of Malta")
    val timeDifferenceText: StateFlow<String> = _timeDifferenceText.asStateFlow()

    // Live Countdown
    private val _countdownRemaining = MutableStateFlow(CountdownRemaining())
    val countdownRemaining: StateFlow<CountdownRemaining> = _countdownRemaining.asStateFlow()

    // Transient UI Events
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    private val _isSendingPing = MutableStateFlow(false)
    val isSendingPing: StateFlow<Boolean> = _isSendingPing.asStateFlow()

    init {
        startClockAndCountdownTicker()
    }

    private fun startClockAndCountdownTicker() {
        viewModelScope.launch {
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
            val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

            val maltaZone = ZoneId.of("Europe/Malta")
            val nepalZone = ZoneId.of("Asia/Kathmandu")

            while (true) {
                val maltaNow = ZonedDateTime.now(maltaZone)
                val nepalNow = ZonedDateTime.now(nepalZone)

                _maltaClock.value = ClockState(
                    timeFormatted = maltaNow.format(timeFormatter),
                    dateFormatted = maltaNow.format(dateFormatter),
                    hourOfDay = maltaNow.hour,
                    isDaytime = maltaNow.hour in 6..19,
                    zoneOffsetFormatted = "UTC" + maltaNow.offset.toString()
                )

                _nepalClock.value = ClockState(
                    timeFormatted = nepalNow.format(timeFormatter),
                    dateFormatted = nepalNow.format(dateFormatter),
                    hourOfDay = nepalNow.hour,
                    isDaytime = nepalNow.hour in 6..19,
                    zoneOffsetFormatted = "UTC" + nepalNow.offset.toString()
                )

                // Calculate difference in minutes
                val diffSeconds = Duration.between(maltaNow.toLocalDateTime(), nepalNow.toLocalDateTime()).seconds
                val diffHours = diffSeconds / 3600
                val diffMins = (diffSeconds % 3600) / 60
                _timeDifferenceText.value = "Nepal is ${diffHours}h ${diffMins}m ahead of Malta"

                // Update Countdown calculation
                val target = countdown.value.targetTimestamp
                val nowMs = System.currentTimeMillis()
                val remainingMs = target - nowMs

                if (remainingMs > 0) {
                    val days = remainingMs / (1000 * 60 * 60 * 24)
                    val hours = (remainingMs / (1000 * 60 * 60)) % 24
                    val minutes = (remainingMs / (1000 * 60)) % 60
                    val seconds = (remainingMs / 1000) % 60
                    val totalSec = remainingMs / 1000

                    _countdownRemaining.value = CountdownRemaining(
                        days = days,
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        isPassed = false,
                        totalSeconds = totalSec
                    )
                } else {
                    _countdownRemaining.value = CountdownRemaining(
                        days = 0,
                        hours = 0,
                        minutes = 0,
                        seconds = 0,
                        isPassed = true,
                        totalSeconds = 0
                    )
                }

                delay(1000)
            }
        }
    }

    fun loginAs(partner: Partner) {
        repository.login(partner)
        viewModelScope.launch {
            _uiEvent.emit("Logged in as ${partner.displayName} (${partner.countryCode})")
        }
    }

    fun logout() {
        repository.logout()
    }

    fun sendThinkingOfYou() {
        viewModelScope.launch {
            _isSendingPing.value = true
            triggerHeartbeatHaptic()
            val result = repository.sendThinkingOfYouPing()
            _isSendingPing.value = false
            if (result.isSuccess) {
                val partnerName = currentPartner.value.otherPartner.displayName
                _uiEvent.emit("Sent a love ping to $partnerName!")
            } else {
                _uiEvent.emit("Love ping saved locally!")
            }
        }
    }

    fun setMood(moodKey: String, label: String, note: String) {
        viewModelScope.launch {
            repository.updateMood(moodKey, label, note)
            _uiEvent.emit("Updated mood: $label")
        }
    }

    fun updateCountdown(title: String, targetTimestamp: Long, category: String, note: String) {
        viewModelScope.launch {
            val event = CountdownEvent(
                id = "primary_countdown",
                title = title,
                targetTimestamp = targetTimestamp,
                category = category,
                note = note
            )
            repository.updateCountdown(event)
            _uiEvent.emit("Countdown updated: $title")
        }
    }

    fun addDiaryEntry(caption: String, imageUri: Uri?, location: String? = null) {
        viewModelScope.launch {
            repository.addDiaryEntry(caption, imageUri, location)
            _uiEvent.emit("Memory added to Shared Diary")
        }
    }

    fun toggleDiaryLove(entryId: String) {
        viewModelScope.launch {
            repository.toggleDiaryLove(entryId)
        }
    }

    fun addLockedNote(title: String, content: String, unlockTimestamp: Long, hint: String) {
        viewModelScope.launch {
            repository.addLockedNote(title, content, unlockTimestamp, hint)
            _uiEvent.emit("Locked note saved!")
        }
    }

    fun revealLockedNote(noteId: String) {
        viewModelScope.launch {
            repository.revealLockedNote(noteId)
            _uiEvent.emit("Note opened!")
        }
    }

    fun savePartnerNames(maltaName: String, nepalName: String) {
        repository.updatePartnerNames(maltaName, nepalName)
        viewModelScope.launch {
            _uiEvent.emit("Saved partner names")
        }
    }

    fun getPartnerNames(): Pair<String, String> {
        return repository.getPartnerNames()
    }

    private fun triggerHeartbeatHaptic() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Vibrator::class.java)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(
                        longArrayOf(0, 120, 80, 180),
                        intArrayOf(0, 200, 0, 255),
                        -1
                    )
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 120, 80, 180), -1)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration fails
        }
    }
}
