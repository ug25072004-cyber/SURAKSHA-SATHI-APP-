package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.PriorityLevel
import com.example.data.model.ZoneTriage
import com.example.ui.theme.LocalEmergencyColors

/**
 * High-Contrast Priority Badge for Instant Glanceability.
 * Uses Red for CRITICAL, Orange for HIGH, Blue for MEDIUM, Magenta for ESCALATED.
 */
@Composable
fun UrgencyBadge(
    priority: PriorityLevel,
    modifier: Modifier = Modifier,
    isEscalated: Boolean = false
) {
    val emergencyColors = LocalEmergencyColors.current

    val (bgColor, textColor, icon) = when {
        isEscalated -> Triple(emergencyColors.magentaContainer, emergencyColors.magentaElectric, Icons.AutoMirrored.Filled.AltRoute)
        priority == PriorityLevel.CRITICAL -> Triple(emergencyColors.redContainer, emergencyColors.redCritical, Icons.Filled.CrisisAlert)
        priority == PriorityLevel.HIGH -> Triple(emergencyColors.orangeContainer, emergencyColors.orangeWarning, Icons.Filled.Warning)
        else -> Triple(emergencyColors.blueContainer, emergencyColors.blueLight, Icons.Filled.Security)
    }

    val labelText = if (isEscalated) "ESCALATED (DYNAMIC)" else priority.label

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = labelText,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Pulsing Dynamic Re-Allocation Alert Banner.
 * Signals commander when the OR-Tools VRP recalculation is triggered by field changes.
 */
@Composable
fun DynamicReallocationBanner(
    isActive: Boolean,
    reason: String,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    AnimatedVisibility(visible = isActive, modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("dynamic_reallocation_banner"),
            colors = CardDefaults.cardColors(containerColor = emergencyColors.magentaContainer),
            border = BorderStroke(1.5.dp, emergencyColors.magentaElectric),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(emergencyColors.magentaTactical),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DYNAMIC RE-ALLOCATION ACTIVE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.magentaElectric
                            )
                            Text(
                                text = "OR-Tools VRP Engine Recalculated",
                                style = MaterialTheme.typography.labelSmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "RESET",
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(emergencyColors.magentaDark)
                            .clickable { onResetClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = emergencyColors.textPrimary
                )
            }
        }
    }
}

/**
 * Triage Zone Card detailing Demand vs Allocated metrics.
 */
@Composable
fun TriageZoneCard(
    zone: ZoneTriage,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {}
) {
    val emergencyColors = LocalEmergencyColors.current

    val cardBorderColor = when (zone.priority) {
        PriorityLevel.CRITICAL -> emergencyColors.redCritical
        PriorityLevel.HIGH -> emergencyColors.orangeWarning
        else -> emergencyColors.blueLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("zone_card_${zone.id}")
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Zone Name + Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textPrimary
                    )
                    Text(
                        text = "${zone.sector} • ${zone.affectedPeople} Citizens",
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textSecondary
                    )
                }
                UrgencyBadge(priority = zone.priority)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Urgency Meter Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Urgency Index",
                    style = MaterialTheme.typography.labelSmall,
                    color = emergencyColors.textSecondary,
                    modifier = Modifier.width(85.dp)
                )
                LinearProgressIndicator(
                    progress = { zone.urgencyRuler },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = cardBorderColor,
                    trackColor = emergencyColors.surfaceBorder.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(zone.urgencyRuler * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = cardBorderColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resource Metrics Grid (Water, Food, Medical, Vehicles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ResourceStat(
                    label = "Water",
                    allocated = zone.allocatedWater,
                    demand = zone.demandWater,
                    icon = Icons.Filled.WaterDrop,
                    tint = emergencyColors.blueLight
                )
                ResourceStat(
                    label = "Rations",
                    allocated = zone.allocatedFood,
                    demand = zone.demandFood,
                    icon = Icons.Filled.Warning,
                    tint = emergencyColors.orangeWarning
                )
                ResourceStat(
                    label = "Medical",
                    allocated = zone.allocatedMedicalTeams,
                    demand = zone.demandMedicalTeams,
                    icon = Icons.Filled.LocalHospital,
                    tint = emergencyColors.redCritical
                )
                ResourceStat(
                    label = "Rescue",
                    allocated = zone.allocatedRescueVehicles,
                    demand = zone.demandRescueVehicles,
                    icon = Icons.Filled.Navigation,
                    tint = emergencyColors.magentaElectric
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Access Status Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(emergencyColors.surfaceBorder.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Access: ${zone.accessStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ResourceStat(
    label: String,
    allocated: Int,
    demand: Int,
    icon: ImageVector,
    tint: Color
) {
    val emergencyColors = LocalEmergencyColors.current
    val isFulfilled = allocated >= demand

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = emergencyColors.textSecondary)
        }
        Text(
            text = "$allocated / $demand",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isFulfilled) emergencyColors.textPrimary else emergencyColors.redCritical
        )
    }
}

/**
 * Emergency Action Button designed for high-stress ergonomics.
 * Enforces >= 56dp height and unmistakable tactile visual states.
 */
@Composable
fun EmergencyActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color = Color.White,
    testTag: String = "emergency_action_button"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
