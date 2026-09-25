package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.DynamicReallocationBanner
import com.example.ui.components.EmergencyActionButton
import com.example.ui.components.SurakshaLogo
import com.example.ui.components.TriageZoneCard
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel

@Composable
fun DashboardScreen(
    viewModel: EmergencyViewModel,
    onNavigateToGis: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToStockpile: () -> Unit = {},
    onNavigateToDirectory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Header & Role Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SurakshaLogo(
                    size = 46.dp,
                    showWordmark = true,
                    tagline = "Emergency Command Center"
                )

                // Role Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(emergencyColors.bluePrimary.copy(alpha = 0.2f))
                        .clickable {
                            val nextRole = when (uiState.currentRole) {
                                UserRole.DISASTER_COMMANDER -> UserRole.RESPONSE_TEAM
                                UserRole.RESPONSE_TEAM -> UserRole.HOSPITAL_LEAD
                                UserRole.HOSPITAL_LEAD -> UserRole.CITIZEN_VOLUNTEER
                                UserRole.CITIZEN_VOLUNTEER -> UserRole.DISASTER_COMMANDER
                            }
                            viewModel.setRole(nextRole)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("role_switcher_pill")
                ) {
                    Text(
                        text = "Role: ${uiState.currentRole.title}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.blueLight
                    )
                }
            }
        }

        // Active Disaster Headline Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, emergencyColors.redCritical.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(emergencyColors.redPulse)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE SITUATIONAL INCIDENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.redCritical,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Smart India Hackathon 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = emergencyColors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.activeDisasterName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = emergencyColors.textPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = emergencyColors.orangeBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = uiState.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = emergencyColors.textSecondary
                        )
                    }

                    if (uiState.isSosProtocolActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(emergencyColors.redCritical)
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚨 SOS: ${uiState.activeSosProtocolName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .clickable { viewModel.dismissSosProtocol() }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "DISMISS",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Status: ${uiState.sosTransmissionStatus} • Lock: ${uiState.sosTelemetryCoords}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Re-Allocation Alert Banner (if active)
        item {
            DynamicReallocationBanner(
                isActive = uiState.isDynamicReallocationActive,
                reason = uiState.reallocationReason,
                onResetClick = { viewModel.resetAllocationToBaseline() }
            )
        }

        // Telemetry Aggregate Metrics (Water, Food, Medical, People)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "Impacted",
                    value = "${uiState.totalPeopleInNeed / 1000}k",
                    sub = "Citizens",
                    icon = Icons.Filled.People,
                    color = emergencyColors.redCritical,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Water",
                    value = "${uiState.totalWaterAllocated}",
                    sub = "Units Allocated",
                    icon = Icons.Filled.WaterDrop,
                    color = emergencyColors.blueLight,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Rations",
                    value = "${uiState.totalFoodAllocated}",
                    sub = "Food Packs",
                    icon = Icons.Filled.Restaurant,
                    color = emergencyColors.orangeWarning,
                    modifier = Modifier.weight(1f)
                )
                DashboardMetricCard(
                    title = "Medical",
                    value = "${uiState.totalMedicalDeployed}",
                    sub = "Teams Deployed",
                    icon = Icons.Filled.LocalHospital,
                    color = emergencyColors.magentaElectric,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Core PRD Feature Action: Dynamic Re-allocation Engine Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI RESOURCE OPTIMIZATION (OR-TOOLS VRP)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.magentaElectric
                        )
                        if (uiState.isSimulatingAllocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = emergencyColors.magentaElectric,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Simulate an unpredicted field disruption (e.g. road submerged or citizen count surge) to trigger automated multi-objective re-allocation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    EmergencyActionButton(
                        text = if (uiState.isDynamicReallocationActive) "RE-OPTIMIZE RESOURCE DISTRIBUTION" else "TRIGGER DYNAMIC RE-ALLOCATION",
                        icon = Icons.AutoMirrored.Filled.AltRoute,
                        onClick = { viewModel.triggerDynamicReallocation() },
                        containerColor = emergencyColors.magentaTactical,
                        testTag = "trigger_dynamic_reallocation_button"
                    )
                }
            }
        }

        // Stockpile & Relief Depots Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToStockpile() }
                    .testTag("card_nav_stockpile"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    if (uiState.stockpileSummary.isCriticalShortagePresent) emergencyColors.redCritical.copy(alpha = 0.6f) else emergencyColors.blueLight.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(emergencyColors.blueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Route,
                                contentDescription = null,
                                tint = emergencyColors.blueLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Relief Depots & Stockpiles",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.textPrimary
                                )
                                if (uiState.stockpileSummary.isCriticalShortagePresent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(emergencyColors.redCritical.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SHORTAGE ALERT",
                                            color = emergencyColors.redCritical,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "4 Regional Depots • ${uiState.stockpileSummary.totalWaterStock} Units Water (${uiState.stockpileSummary.hoursRemainingWater.toInt()}h Runway)",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "MANAGE →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.blueLight
                    )
                }
            }
        }

        // Offline Emergency Contacts & Critical Resources Directory Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDirectory() }
                    .testTag("card_nav_offline_directory"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, emergencyColors.greenStable.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(emergencyColors.greenStable.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContactPhone,
                                contentDescription = null,
                                tint = emergencyColors.greenStable,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Offline Emergency Directory",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(emergencyColors.greenStable.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "100% OFFLINE",
                                        color = emergencyColors.greenStable,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.offlineContacts.size} Emergency Contacts • ${uiState.offlineResources.size} Life-Saving Assets",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }

                    Text(
                        text = "VIEW →",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.greenStable
                    )
                }
            }
        }

        // Quick Navigation to GIS Route & AI Assistant
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGis() }
                        .testTag("card_nav_gis"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, emergencyColors.blueLight.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Route, contentDescription = null, tint = emergencyColors.blueLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GIS Route Map",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = "View live convoy corridors",
                                style = MaterialTheme.typography.labelSmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToAi() }
                        .testTag("card_nav_ai"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, emergencyColors.magentaElectric.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.SmartToy, contentDescription = null, tint = emergencyColors.magentaElectric)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Sathi Tactical AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = "High thinking copilot",
                                style = MaterialTheme.typography.labelSmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Triage Zone List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AFFECTED ZONE TRIAGE MATRIX",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${uiState.zones.size} Active Sectors",
                    style = MaterialTheme.typography.labelSmall,
                    color = emergencyColors.textSecondary
                )
            }
        }

        // Triage Zone Cards
        items(uiState.zones) { zone ->
            TriageZoneCard(zone = zone, onCardClick = onNavigateToGis)
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = emergencyColors.textPrimary
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = emergencyColors.textSecondary,
                maxLines = 1
            )
        }
    }
}
