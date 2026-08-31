package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Partner
import com.example.service.MilanKeepAliveService
import com.example.service.MilanSyncWorker
import com.example.service.NotificationHelper
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.PinkHeart
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleViolet400
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentPartner: Partner,
    isFirebaseActive: Boolean,
    onSwitchPartner: (Partner) -> Unit,
    onLogout: () -> Unit,
    onSaveNames: (maltaName: String, nepalName: String) -> Unit,
    initialMaltaName: String,
    initialNepalName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditNamesDialog by remember { mutableStateOf(false) }
    var showFirebaseInfoDialog by remember { mutableStateOf(false) }
    var showBatteryGuideDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings & Connection",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Current Active Account Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                color = DarkSurface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(1.5.dp, PurpleLight, CircleShape)
                            ) {
                                Text(
                                    text = currentPartner.countryCode,
                                    color = PurpleLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = currentPartner.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${currentPartner.cityName}, ${currentPartner.countryName}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        // Switch Partner Button
                        Button(
                            onClick = { onSwitchPartner(currentPartner.otherPartner) },
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("switch_partner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch",
                                tint = PurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", color = PurpleLight, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Couple Milestones & Distance Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                color = DarkSurface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Love Across Borders",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatPill(
                            title = "Distance",
                            value = "6,800 km",
                            icon = Icons.Default.FlightTakeoff,
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            title = "Time Offset",
                            value = "+3h 45m",
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatPill(
                            title = "Malta",
                            value = "Valletta (MLT)",
                            icon = Icons.Default.LocationOn,
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            title = "Nepal",
                            value = "Kathmandu (NPL)",
                            icon = Icons.Default.LocationCity,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Options List
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                color = DarkSurface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingRow(
                        icon = Icons.Default.Edit,
                        title = "Customize Display Names",
                        subtitle = "$initialMaltaName & $initialNepalName",
                        onClick = { showEditNamesDialog = true }
                    )

                    SettingRow(
                        icon = Icons.Default.CloudDone,
                        title = "Firebase Cloud Integration",
                        subtitle = if (isFirebaseActive) "Connected with Firestore & FCM" else "Ready for google-services.json",
                        onClick = { showFirebaseInfoDialog = true }
                    )

                    SettingRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Test Notification Chime",
                        subtitle = "Play custom 'Thinking of You' sound & preview alert",
                        onClick = {
                            NotificationHelper.playPingSound(context)
                            NotificationHelper.showThinkingOfYouNotification(
                                context = context,
                                senderName = if (currentPartner == Partner.MALTA) initialNepalName else initialMaltaName,
                                message = "Thinking of you right now across the distance!"
                            )
                        }
                    )

                    SettingRow(
                        icon = Icons.Default.Notifications,
                        title = "Push Notification Channels",
                        subtitle = "Custom chime sound, heartbeat vibration & heads-up alerts",
                        onClick = {
                            NotificationHelper.createNotificationChannels(context)
                            NotificationHelper.playPingSound(context)
                        }
                    )

                    SettingRow(
                        icon = Icons.Default.Sync,
                        title = "Background Real-Time Connection",
                        subtitle = "Keeps live cloud ping sync active even when app is closed",
                        onClick = {
                            MilanKeepAliveService.start(context)
                            MilanSyncWorker.schedulePeriodicSync(context)
                            NotificationHelper.playPingSound(context)
                        }
                    )

                    SettingRow(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Background Alert & Battery Guide",
                        subtitle = "Prevent Android from putting Milan to sleep",
                        onClick = { showBatteryGuideDialog = true }
                    )

                    SettingRow(
                        icon = Icons.Default.Logout,
                        title = "Sign Out",
                        subtitle = "Return to Profile selection",
                        onClick = onLogout,
                        tint = PinkHeart
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Dialog for Customizing Partner Nicknames
        if (showEditNamesDialog) {
            EditNicknamesDialog(
                initialMaltaName = initialMaltaName,
                initialNepalName = initialNepalName,
                onDismiss = { showEditNamesDialog = false },
                onSave = { malta, nepal ->
                    onSaveNames(malta, nepal)
                    showEditNamesDialog = false
                }
            )
        }

        // Dialog for Firebase Information
        if (showFirebaseInfoDialog) {
            AlertDialog(
                onDismissRequest = { showFirebaseInfoDialog = false },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Firebase Setup Guide",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Milan is architected to seamlessly connect with Firebase:\n\n" +
                                    "1. Place your 'google-services.json' in the app directory\n" +
                                    "2. Package name: 'com.aistudio.milan.pnktvx'\n" +
                                    "3. Firestore collections used: 'pings', 'moods', 'countdowns', 'diary_entries', 'locked_notes'\n" +
                                    "4. Push notifications are routed through Firebase Cloud Messaging (FCM).\n\n" +
                                    "Offline caching via Room database ensures all memories are preserved even without internet!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showFirebaseInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Got it!")
                    }
                }
            )
        }

        // Dialog for Background & Battery Optimization Guide
        if (showBatteryGuideDialog) {
            AlertDialog(
                onDismissRequest = { showBatteryGuideDialog = false },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Instant Alerts When Closed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "To ensure you get instant 'Thinking of You' pings and memories even when Milan is closed or swiped away:\n\n" +
                                    "1. App Settings: Long press the Milan app icon > App Info > Battery > Select 'Unrestricted' (or 'No restrictions').\n\n" +
                                    "2. Autostart (Xiaomi / Redmi / POCO): Enable 'Autostart' in App Info.\n\n" +
                                    "3. Notifications: Ensure 'All Notifications' and 'Pop on screen / Heads-up' are enabled.\n\n" +
                                    "4. Push Engine: Milan utilizes direct FCM High-Priority Push + periodic WorkManager background sync so pings reach your partner immediately!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showBatteryGuideDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(0.5.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PurpleViolet400,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = PurpleLight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun EditNicknamesDialog(
    initialMaltaName: String,
    initialNepalName: String,
    onDismiss: () -> Unit,
    onSave: (malta: String, nepal: String) -> Unit
) {
    var maltaName by remember { mutableStateOf(initialMaltaName) }
    var nepalName by remember { mutableStateOf(initialNepalName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Edit Partner Names",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = maltaName,
                    onValueChange = { maltaName = it },
                    label = { Text("Anish (Malta)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nepalName,
                    onValueChange = { nepalName = it },
                    label = { Text("Puri (Nepal)", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = DarkBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(maltaName, nepalName) },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Save Names")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
