package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.service.NotificationHelper
import com.example.ui.components.MilanBottomBar
import com.example.ui.components.MilanScreen
import com.example.ui.screens.DiaryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockedNotesScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MilanTheme
import com.example.viewmodel.MilanViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MilanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create high-importance push notification channels
        NotificationHelper.createNotificationChannels(this)

        setContent {
            MilanTheme {
                RequestNotificationPermission()

                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val currentPartner by viewModel.currentPartner.collectAsState()
                val isFirebaseActive by viewModel.isFirebaseActive.collectAsState()

                val maltaClock by viewModel.maltaClock.collectAsState()
                val nepalClock by viewModel.nepalClock.collectAsState()
                val timeDifferenceText by viewModel.timeDifferenceText.collectAsState()

                val lastPing by viewModel.lastPing.collectAsState()
                val isSendingPing by viewModel.isSendingPing.collectAsState()
                val partnerMood by viewModel.partnerMood.collectAsState()
                val myMood by viewModel.myMood.collectAsState()

                val countdown by viewModel.countdown.collectAsState()
                val countdownRemaining by viewModel.countdownRemaining.collectAsState()

                val diaryEntries by viewModel.diaryEntries.collectAsState()
                val lockedNotes by viewModel.lockedNotes.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                var currentScreen by remember { mutableStateOf(MilanScreen.HOME) }

                val partnerNames = viewModel.getPartnerNames()

                // Collect transient UI Events for Toast / Snackbar messages
                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginPartner = { partner ->
                            viewModel.loginAs(partner)
                        }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            MilanBottomBar(
                                currentRoute = currentScreen.route,
                                onNavigate = { screen ->
                                    currentScreen = screen
                                }
                            )
                        },
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        }
                    ) { innerPadding ->
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_navigation",
                            modifier = Modifier.padding(innerPadding)
                        ) { screen ->
                            when (screen) {
                                MilanScreen.HOME -> {
                                    HomeScreen(
                                        currentPartner = currentPartner,
                                        isFirebaseActive = isFirebaseActive,
                                        maltaClock = maltaClock,
                                        nepalClock = nepalClock,
                                        timeDifferenceText = timeDifferenceText,
                                        lastPing = lastPing,
                                        isSendingPing = isSendingPing,
                                        partnerMood = partnerMood,
                                        myMood = myMood,
                                        countdown = countdown,
                                        countdownRemaining = countdownRemaining,
                                        onSendPing = { viewModel.sendThinkingOfYou() },
                                        onSetMood = { emoji, label, note ->
                                            viewModel.setMood(emoji, label, note)
                                        },
                                        onUpdateCountdown = { title, target, category, note ->
                                            viewModel.updateCountdown(title, target, category, note)
                                        }
                                    )
                                }
                                MilanScreen.DIARY -> {
                                    DiaryScreen(
                                        currentPartner = currentPartner,
                                        entries = diaryEntries,
                                        onAddEntry = { caption, uri, location ->
                                            viewModel.addDiaryEntry(caption, uri, location)
                                        },
                                        onToggleLove = { entryId ->
                                            viewModel.toggleDiaryLove(entryId)
                                        }
                                    )
                                }
                                MilanScreen.NOTES -> {
                                    LockedNotesScreen(
                                        currentPartner = currentPartner,
                                        notes = lockedNotes,
                                        onAddNote = { title, content, unlockTime, hint ->
                                            viewModel.addLockedNote(title, content, unlockTime, hint)
                                        },
                                        onRevealNote = { noteId ->
                                            viewModel.revealLockedNote(noteId)
                                        }
                                    )
                                }
                                MilanScreen.SETTINGS -> {
                                    SettingsScreen(
                                        currentPartner = currentPartner,
                                        isFirebaseActive = isFirebaseActive,
                                        onSwitchPartner = { newPartner ->
                                            viewModel.loginAs(newPartner)
                                        },
                                        onLogout = { viewModel.logout() },
                                        onSaveNames = { malta, nepal ->
                                            viewModel.savePartnerNames(malta, nepal)
                                        },
                                        initialMaltaName = partnerNames.first,
                                        initialNepalName = partnerNames.second
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Permission outcome handled
            }

            LaunchedEffect(Unit) {
                val permission = Manifest.permission.POST_NOTIFICATIONS
                if (ContextCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionLauncher.launch(permission)
                }
            }
        }
    }
}
