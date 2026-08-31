package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThinkingOfYouPing
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet300
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ThinkingOfYouButton(
    lastPing: ThinkingOfYouPing?,
    isSending: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val tapScale = remember { Animatable(1f) }

    // Breathing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "vibrant_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Outermost pulsing halo (w-72 h-72 bg-violet-600/10 animate-ping)
            Box(
                modifier = Modifier
                    .size(236.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = haloAlpha * 0.4f))
            )

            // Middle blurred soft glow (w-64 h-64 bg-violet-500/20 blur-xl)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(PurpleLight.copy(alpha = haloAlpha * 0.6f))
            )

            // Core Action Button (w-56 h-56 rounded-full bg-[#8B5CF6] shadow-[0_0_60px_-15px_rgba(139,92,246,0.5)] border-4 border-violet-400/30)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(175.dp)
                    .scale(tapScale.value)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        spotColor = PurplePrimary.copy(alpha = 0.8f),
                        ambientColor = PurplePrimary
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PurplePrimary,
                                Color(0xFF7C3AED)
                            )
                        )
                    )
                    .border(3.5.dp, PurpleLight.copy(alpha = 0.35f), CircleShape)
                    .testTag("thinking_of_you_button")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White, radius = 90.dp)
                    ) {
                        coroutineScope.launch {
                            tapScale.animateTo(0.92f, spring(dampingRatio = 0.4f, stiffness = 400f))
                            tapScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f))
                        }
                        onTap()
                    }
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        // Heart Icon (svg fill-white/90)
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.95f),
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Text: "Thinking of You" (text-lg font-bold leading-tight)
                        Text(
                            text = "Thinking of You",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Subtitle: "SEND INSTANT HUG" (text-[10px] text-violet-100/70 font-medium uppercase tracking-widest)
                        Text(
                            text = "SEND INSTANT HUG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PurpleViolet300.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status text below button (text-xs text-slate-500 font-medium)
        val lastPingTime = lastPing?.let { formatRelativeTime(it.timestamp) }
        Text(
            text = if (lastPingTime != null) {
                "Last ping: ${lastPing.senderDisplayName} ($lastPingTime)"
            } else {
                "Connected across 6,800 km"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (lastPingTime != null) PurpleViolet300 else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diffSec = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diffSec < 60 -> "just now"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}

