package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ClockState

@Composable
fun LiveClockCard(
    maltaClock: ClockState,
    nepalClock: ClockState,
    timeDifferenceText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle connection difference badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = timeDifferenceText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PurpleViolet400,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of 2 rounded-[2rem] cards from Vibrant Palette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Malta Card
            VibrantClockCard(
                countryCode = "MLT",
                country = "MALTA",
                city = "Valletta",
                clock = maltaClock,
                modifier = Modifier.weight(1f)
            )

            // Nepal Card
            VibrantClockCard(
                countryCode = "NPL",
                country = "NEPAL",
                city = "Kathmandu",
                clock = nepalClock,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VibrantClockCard(
    countryCode: String,
    country: String,
    city: String,
    clock: ClockState,
    modifier: Modifier = Modifier
) {
    // Parse time and am/pm if present, or fallback gracefully
    val rawTime = clock.timeFormatted
    val parts = rawTime.split(" ")
    val timeDigits = parts.firstOrNull() ?: rawTime
    val period = if (parts.size > 1) parts[1] else if (clock.isDaytime) "DAY" else "NIGHT"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(32.dp))
            .padding(vertical = 18.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Country Label (text-[10px] uppercase tracking-widest text-violet-400 font-bold mb-1)
            Text(
                text = "$country • $countryCode",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PurpleViolet400,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Time: text-2xl font-light tracking-tighter
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeDigits,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp,
                        letterSpacing = (-1).sp
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // City + Daytime / Night icon: text-[10px] opacity-40 mt-1 font-medium
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = city,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.45f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (clock.isDaytime) Icons.Default.WbSunny else Icons.Default.Nightlight,
                    contentDescription = if (clock.isDaytime) "Day" else "Night",
                    tint = if (clock.isDaytime) GoldStar else PurpleLight,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

