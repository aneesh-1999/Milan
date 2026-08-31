package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet300
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CountdownRemaining

@Composable
fun CountdownCard(
    countdown: CountdownEvent,
    remaining: CountdownRemaining,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Section from Vibrant Palette: bg-gradient-to-r from-violet-600/20 to-transparent p-5 rounded-[2.5rem] border border-violet-500/20
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        PurplePrimary.copy(alpha = 0.20f),
                        Color(0xFF1E1B24).copy(alpha = 0.90f),
                        Color(0xFF1E1B24).copy(alpha = 0.60f)
                    )
                )
            )
            .border(1.dp, PurplePrimary.copy(alpha = 0.25f), RoundedCornerShape(36.dp))
            .clickable { onEditClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag("edit_countdown_button")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Header (text-[11px] font-bold text-violet-300 uppercase tracking-[0.15em] block mb-1)
                Text(
                    text = countdown.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PurpleViolet300,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.6.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time Readout: text-xl font-light tracking-tight text-white flex items-baseline gap-2
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${remaining.days}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Days",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Text(
                        text = String.format("%02d", remaining.hours),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Hours",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    Text(
                        text = String.format("%02d", remaining.minutes),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Mins",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )
                }

                if (countdown.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = countdown.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Flight / Reunion icon in circular backdrop (w-12 h-12 bg-white/5 rounded-full flex items-center justify-center backdrop-blur-md text-violet-400)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FlightTakeoff,
                    contentDescription = "Edit Countdown",
                    tint = PurpleViolet400,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

