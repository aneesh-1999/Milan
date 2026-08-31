package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountdownEvent
import com.example.model.MoodEntry
import com.example.model.Partner
import com.example.model.ThinkingOfYouPing
import com.example.ui.components.CountdownCard
import com.example.ui.components.CountdownEditDialog
import com.example.ui.components.LiveClockCard
import com.example.ui.components.MoodSelectorBar
import com.example.ui.components.ThinkingOfYouButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.FuchsiaAccent
import com.example.ui.theme.PinkHeart
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet300
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ClockState
import com.example.viewmodel.CountdownRemaining

@Composable
fun HomeScreen(
    currentPartner: Partner,
    isFirebaseActive: Boolean,
    maltaClock: ClockState,
    nepalClock: ClockState,
    timeDifferenceText: String,
    lastPing: ThinkingOfYouPing?,
    isSendingPing: Boolean,
    partnerMood: MoodEntry?,
    myMood: MoodEntry?,
    countdown: CountdownEvent,
    countdownRemaining: CountdownRemaining,
    onSendPing: () -> Unit,
    onSetMood: (moodKey: String, label: String, note: String) -> Unit,
    onUpdateCountdown: (title: String, targetTimestamp: Long, category: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditCountdownDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Vibrant Palette Header with Connection Subtitle & Partner Status Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Subtitle + Milan Title
                Column {
                    Text(
                        text = "CONNECTED SINCE 2022",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PurpleViolet400.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Milan",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                }

                // Right: Partner Status Pill
                val partner = currentPartner.otherPartner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(32.dp))
                        .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    // Avatar circle with gradient from violet-500 to fuchsia-500
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        PurplePrimary,
                                        FuchsiaAccent
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Text(
                            text = partner.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = partner.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextPrimary.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = partnerMood?.moodLabel ?: "Peaceful",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PurpleViolet300,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 1. Dual Live Clocks for Malta & Nepal
            LiveClockCard(
                maltaClock = maltaClock,
                nepalClock = nepalClock,
                timeDifferenceText = timeDifferenceText,
                modifier = Modifier.testTag("live_clock_card")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Large Circular "Thinking of You" Button
            ThinkingOfYouButton(
                lastPing = lastPing,
                isSending = isSendingPing,
                onTap = onSendPing,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Countdown Widget
            CountdownCard(
                countdown = countdown,
                remaining = countdownRemaining,
                onEditClick = { showEditCountdownDialog = true },
                modifier = Modifier.testTag("countdown_card")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Mood Check-in Bar
            MoodSelectorBar(
                currentPartner = currentPartner,
                partnerMood = partnerMood,
                myMood = myMood,
                onSelectMood = onSetMood,
                modifier = Modifier.testTag("mood_selector_bar")
            )

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Edit Countdown Dialog
        if (showEditCountdownDialog) {
            CountdownEditDialog(
                currentCountdown = countdown,
                onDismiss = { showEditCountdownDialog = false },
                onSave = { title, targetTimestamp, category, note ->
                    onUpdateCountdown(title, targetTimestamp, category, note)
                    showEditCountdownDialog = false
                }
            )
        }
    }
}
