package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmergencyRedCritical
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.EmergencyRedPulse
import com.example.ui.theme.LocalEmergencyColors
import kotlinx.coroutines.delay

/**
 * Highly visible 'SOS Emergency' Floating Action Button (FAB).
 * Uses the specified emergency red palette (EmergencyRedCritical #D32F2F and EmergencyRedPulse #FF1744)
 * with animated dual-ring radar beacon pulse, tactile 64dp touch target, and high-contrast styling.
 */
@Composable
fun SosEmergencyFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBroadcasting: Boolean = false
) {
    val emergencyColors = LocalEmergencyColors.current

    // Pulsing Radar Rings Animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos_beacon_rings")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 1.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(96.dp)
            .testTag("sos_fab_container")
    ) {
        // Outer Radar Pulse Ring 2
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(pulseScale2)
                .clip(CircleShape)
                .background(EmergencyRedPulse.copy(alpha = pulseAlpha2))
        )

        // Outer Radar Pulse Ring 1
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(pulseScale1)
                .clip(CircleShape)
                .background(EmergencyRedPulse.copy(alpha = pulseAlpha1))
        )

        // Primary Tactical Floating Action Button
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(68.dp)
                .testTag("sos_emergency_fab")
                .border(
                    BorderStroke(2.5.dp, EmergencyRedPulse),
                    CircleShape
                ),
            shape = CircleShape,
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 10.dp,
                pressedElevation = 4.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                EmergencyRedPulse,
                                EmergencyRedCritical,
                                EmergencyRedDark
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CrisisAlert,
                        contentDescription = "SOS Emergency Trigger",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isBroadcasting) "LIVE" else "SOS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * Emergency Protocol Model for Selection in Protocol Flow.
 */
data class EmergencyProtocolItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val priorityCode: String,
    val dispatchTarget: String
)

/**
 * Immediate Emergency Protocol Dialog Flow.
 * Activated by the SOS FAB to execute life-safety broadcasts, medical evacuations,
 * flood alarms, or search & rescue helicopter winching.
 */
@Composable
fun EmergencyProtocolDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onExecuteProtocol: (protocolName: String, details: String) -> Unit,
    isAlreadyActive: Boolean = false,
    activeProtocolName: String? = null,
    onCancelActiveSos: () -> Unit = {}
) {
    if (!isOpen) return

    val emergencyColors = LocalEmergencyColors.current
    val scrollState = rememberScrollState()

    var selectedProtocol by remember {
        mutableStateOf<EmergencyProtocolItem?>(null)
    }

    var countdownSeconds by remember { mutableIntStateOf(5) }
    var isCountdownRunning by remember { mutableStateOf(false) }

    val protocols = remember {
        listOf(
            EmergencyProtocolItem(
                id = "PROTO-1",
                title = "Level 1: Distress SITREP & GPS Beacon",
                description = "Broadcasts exact coordinate lock to NDRF HQ, SDRF, and District Emergency Operations Net.",
                icon = Icons.Filled.GpsFixed,
                priorityCode = "CRITICAL-01",
                dispatchTarget = "State Disaster Command"
            ),
            EmergencyProtocolItem(
                id = "PROTO-2",
                title = "Level 2: Medical Evac (MEDEVAC)",
                description = "Requests urgent ALS ambulance, mass-casualty triage kits, and flood rescue zodiacs.",
                icon = Icons.Filled.LocalHospital,
                priorityCode = "CRITICAL-02",
                dispatchTarget = "District Trauma Center"
            ),
            EmergencyProtocolItem(
                id = "PROTO-3",
                title = "Level 3: Breach & Flash Flood Siren Alarm",
                description = "Triggers localized acoustic warning towers & cell-broadcast SMS alerts across Wards 1 to 4.",
                icon = Icons.Filled.NotificationsActive,
                priorityCode = "CRITICAL-03",
                dispatchTarget = "Community Siren Net"
            ),
            EmergencyProtocolItem(
                id = "PROTO-4",
                title = "Level 4: Helicopter Airlift / Extraction",
                description = "Immediate rooftop winch extraction request to Coast Guard & Air Force SAR units.",
                icon = Icons.Filled.AirplanemodeActive,
                priorityCode = "CRITICAL-04",
                dispatchTarget = "Coast Guard SAR Wing"
            )
        )
    }

    // Auto-countdown effect once a protocol is selected
    LaunchedEffect(isCountdownRunning, countdownSeconds) {
        if (isCountdownRunning && countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        } else if (isCountdownRunning && countdownSeconds == 0) {
            isCountdownRunning = false
            selectedProtocol?.let { proto ->
                onExecuteProtocol(proto.title, "Auto-transmitted via 5-second countdown. Target: ${proto.dispatchTarget}")
            }
        }
    }

    Dialog(
        onDismissRequest = {
            isCountdownRunning = false
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, EmergencyRedCritical, RoundedCornerShape(20.dp))
                .testTag("emergency_protocol_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header with Distress Beacon Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmergencyRedCritical),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CrisisAlert,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EMERGENCY PROTOCOL FLOW",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = EmergencyRedCritical,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "High-Priority Life-Safety Transmit Net",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            isCountdownRunning = false
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_protocol_dialog_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = emergencyColors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS & Satellite Telemetry Lock Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = emergencyColors.surfaceBorder.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "COORDINATE LOCK: 19.8241° N, 85.8315° E",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = "Altitude: 3.8m MSL • NavIC / 4G Uplink Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GPS FIXED",
                                color = Color(0xFF00E676),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // If SOS is already broadcasting
                if (isAlreadyActive) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = emergencyColors.redContainer),
                        border = BorderStroke(1.5.dp, EmergencyRedCritical),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "BEACON CURRENTLY BROADCASTING",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmergencyRedCritical
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Active Protocol: ${activeProtocolName ?: "High Distress Beacon"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = {
                                    onCancelActiveSos()
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cancel_active_sos_button"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = EmergencyRedDark,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("CANCEL & ABORT BROADCAST", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT IMMEDIATE ACTION PROTOCOL:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selectable Protocols List
                protocols.forEach { protocol ->
                    val isSelected = selectedProtocol?.id == protocol.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) EmergencyRedCritical else emergencyColors.surfaceBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedProtocol = protocol
                                countdownSeconds = 5
                                isCountdownRunning = true
                            }
                            .testTag("protocol_item_${protocol.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) emergencyColors.redContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) EmergencyRedCritical else emergencyColors.surfaceBorder.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = protocol.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else emergencyColors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = protocol.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) EmergencyRedCritical else emergencyColors.textPrimary
                                )
                                Text(
                                    text = protocol.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = emergencyColors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Countdown / Transmission Action Section
                if (isCountdownRunning && selectedProtocol != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = emergencyColors.redContainer),
                        border = BorderStroke(1.5.dp, EmergencyRedPulse),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AUTOMATIC TRANSMISSION IN $countdownSeconds SECONDS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = EmergencyRedPulse,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { countdownSeconds / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = EmergencyRedPulse,
                                trackColor = emergencyColors.surfaceBorder
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        isCountdownRunning = false
                                        selectedProtocol = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("abort_countdown_button"),
                                    border = BorderStroke(1.dp, emergencyColors.surfaceBorder)
                                ) {
                                    Text("ABORT", fontWeight = FontWeight.Bold, color = emergencyColors.textPrimary)
                                }

                                EmergencyActionButton(
                                    text = "TRANSMIT NOW",
                                    icon = Icons.Filled.CrisisAlert,
                                    onClick = {
                                        isCountdownRunning = false
                                        selectedProtocol?.let { proto ->
                                            onExecuteProtocol(proto.title, "Immediate manual transmit. Target: ${proto.dispatchTarget}")
                                        }
                                    },
                                    modifier = Modifier.weight(1.4f),
                                    containerColor = EmergencyRedCritical,
                                    testTag = "transmit_now_button"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
