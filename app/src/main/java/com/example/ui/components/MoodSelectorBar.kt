package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MoodEntry
import com.example.model.Partner
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

fun getMoodIcon(moodKey: String): ImageVector {
    return when (moodKey.lowercase()) {
        "loved" -> Icons.Default.Favorite
        "missing_you" -> Icons.Default.FavoriteBorder
        "happy" -> Icons.Default.SentimentSatisfied
        "busy" -> Icons.Default.WorkOutline
        "sleepy" -> Icons.Default.Bedtime
        "down" -> Icons.Default.CloudQueue
        "excited" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Favorite
    }
}

@Composable
fun MoodSelectorBar(
    currentPartner: Partner,
    partnerMood: MoodEntry?,
    myMood: MoodEntry?,
    onSelectMood: (moodKey: String, label: String, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(32.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Partner's Real-time Mood Status
            val other = currentPartner.otherPartner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PurplePrimary.copy(alpha = 0.18f),
                                DarkSurfaceElevated.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.dp, PurplePrimary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Partner Mood in Glowing Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary.copy(alpha = 0.35f))
                        .border(1.dp, PurpleViolet400.copy(alpha = 0.5f), CircleShape)
                ) {
                    val icon = getMoodIcon(partnerMood?.moodKey ?: "loved")
                    Icon(
                        imageVector = icon,
                        contentDescription = partnerMood?.moodLabel ?: "Mood",
                        tint = PurpleViolet400,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${other.displayName}'s Status (${other.countryCode})".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PurpleViolet400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = partnerMood?.moodLabel?.let { "$it • \"${partnerMood.note.ifBlank { "Feeling connected to you" }}\"" }
                            ?: "Connected & peaceful today",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // My Mood Title
            Text(
                text = "Share your mood with ${other.displayName}:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Mood Picker
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MoodEntry.PRESET_MOODS.forEach { option ->
                    val isSelected = myMood?.moodKey == option.moodKey
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) PurpleViolet400 else DarkBorder,
                        animationSpec = spring(),
                        label = "mood_border"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PurplePrimary.copy(alpha = 0.3f) else DarkSurfaceElevated,
                        animationSpec = spring(),
                        label = "mood_bg"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .clickable {
                                onSelectMood(option.moodKey, option.label, option.subtitle)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("mood_option_${option.label.lowercase().replace(" ", "_")}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getMoodIcon(option.moodKey),
                                contentDescription = option.label,
                                tint = if (isSelected) Color.White else PurpleViolet400,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

