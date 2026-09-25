package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AllocationTransaction
import com.example.data.model.DepotStatus
import com.example.data.model.ReliefDepot
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel

private val EmergencyGreenStable = Color(0xFF2E7D32)

@Composable
fun StockpileDashboardScreen(
    viewModel: EmergencyViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    val summary = uiState.stockpileSummary

    var showManualDispatchDialog by remember { mutableStateOf(false) }
    var selectedFilterDepotId by remember { mutableStateOf<String?>(null) }
    var restockTargetDepot by remember { mutableStateOf<ReliefDepot?>(null) }

    val filteredDepots = remember(uiState.depots, selectedFilterDepotId) {
        if (selectedFilterDepotId == null) {
            uiState.depots
        } else {
            uiState.depots.filter { it.id == selectedFilterDepotId }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STOCKPILE & RELIEF DEPOTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.blueLight,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Resource Logistics Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = emergencyColors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(emergencyColors.blueContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Sensors,
                            contentDescription = null,
                            tint = emergencyColors.blueLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE AUDIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.blueLight
                        )
                    }
                }
            }
        }

        // Critical Depletion Alert Banner (if applicable)
        if (summary.isCriticalShortagePresent) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("critical_stockpile_alert_banner"),
                    colors = CardDefaults.cardColors(containerColor = emergencyColors.redContainer),
                    border = BorderStroke(1.dp, emergencyColors.redCritical.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(emergencyColors.redPulse)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CRITICAL LOGISTICS SHORTAGE DETECTED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = emergencyColors.redCritical,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delta Advance Post (DEPOT-04) & Coastal Staging (DEPOT-02) have dipped below minimum emergency thresholds. Potable water buffer < 15%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = emergencyColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.runAutomatedResourceAllocation() },
                            colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.redCritical),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("resolve_shortage_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Memory, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRIGGER ALLOCATION ENGINE REBALANCING",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Aggregate Stockpile Health Metrics
        item {
            Text(
                text = "Consolidated Regional Stockpiles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = emergencyColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StockpileMetricChip(
                    title = "Drinking Water",
                    current = summary.totalWaterStock,
                    capacity = summary.totalWaterCapacity,
                    unit = "Units",
                    icon = Icons.Filled.WaterDrop,
                    accentColor = emergencyColors.blueLight,
                    badgeText = "${summary.hoursRemainingWater.toInt()}h Runway",
                    testTag = "metric_water"
                )
                StockpileMetricChip(
                    title = "Ration Packs",
                    current = summary.totalRationsStock,
                    capacity = summary.totalRationsCapacity,
                    unit = "Packs",
                    icon = Icons.Filled.Restaurant,
                    accentColor = emergencyColors.orangeBright,
                    badgeText = "Calorie Safe",
                    testTag = "metric_rations"
                )
                StockpileMetricChip(
                    title = "Trauma Med-Kits",
                    current = summary.totalMedicalStock,
                    capacity = summary.totalMedicalCapacity,
                    unit = "Kits",
                    icon = Icons.Filled.LocalHospital,
                    accentColor = emergencyColors.magentaElectric,
                    badgeText = "ALS Standard",
                    testTag = "metric_medical"
                )
                StockpileMetricChip(
                    title = "Zodiac Rescue Boats",
                    current = summary.totalRescueBoatsStock,
                    capacity = 57,
                    unit = "Boats",
                    icon = Icons.Filled.DirectionsBoat,
                    accentColor = emergencyColors.blueLight,
                    badgeText = "Extraction Ready",
                    testTag = "metric_boats"
                )
                StockpileMetricChip(
                    title = "Emergency Fuel",
                    current = summary.totalFuelLiters,
                    capacity = 11300,
                    unit = "Liters",
                    icon = Icons.Filled.LocalGasStation,
                    accentColor = emergencyColors.orangeWarning,
                    badgeText = "Gen-Sets Active",
                    testTag = "metric_fuel"
                )
            }
        }

        // Section 2: Resource Allocation Engine Control Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("allocation_engine_hub_card"),
                colors = CardDefaults.cardColors(containerColor = emergencyColors.magentaContainer),
                border = BorderStroke(1.dp, emergencyColors.magentaTactical.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Memory,
                                contentDescription = null,
                                tint = emergencyColors.magentaElectric,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Resource Allocation Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = emergencyColors.textPrimary
                            )
                        }

                        Text(
                            text = "OR-Tools VRP",
                            style = MaterialTheme.typography.labelSmall,
                            color = emergencyColors.magentaElectric,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Computes multi-objective Pareto-optimal allocation matching zone casualty triage with nearest surplus depots, avoiding stockouts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textSecondary
                    )

                    uiState.lastAllocationEngineMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "⚡ $msg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = emergencyColors.magentaElectric
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runAutomatedResourceAllocation() },
                            enabled = !uiState.isAllocationEngineRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.magentaTactical),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_run_allocation_optimizer"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (uiState.isAllocationEngineRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SOLVING...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AUTO-OPTIMIZE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showManualDispatchDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = emergencyColors.textPrimary),
                            border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_manual_dispatch_open"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = emergencyColors.blueLight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MANUAL DISPATCH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: Relief Depots Directory
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regional Relief Depots (${uiState.depots.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Depot Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DepotFilterChip(
                    text = "All Depots",
                    isSelected = selectedFilterDepotId == null,
                    onClick = { selectedFilterDepotId = null },
                    testTag = "filter_depot_all"
                )
                uiState.depots.forEach { depot ->
                    DepotFilterChip(
                        text = depot.name.split("-").first().trim(),
                        isSelected = selectedFilterDepotId == depot.id,
                        onClick = { selectedFilterDepotId = depot.id },
                        isWarning = depot.status == DepotStatus.CRITICAL_SHORTAGE || depot.status == DepotStatus.DEPLETION_ALERT,
                        testTag = "filter_depot_${depot.id}"
                    )
                }
            }
        }

        // Depots List Cards
        items(filteredDepots, key = { it.id }) { depot ->
            ReliefDepotCard(
                depot = depot,
                onRestockClick = { restockTargetDepot = depot },
                onDispatchClick = { showManualDispatchDialog = true }
            )
        }

        // Section 4: Live Allocation Transactions Manifest
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Dispatch Manifest (Room Audit)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary
                )
                Text(
                    text = "${uiState.allocationHistory.size} Records",
                    style = MaterialTheme.typography.labelSmall,
                    color = emergencyColors.textSecondary
                )
            }
        }

        items(uiState.allocationHistory, key = { it.id }) { tx ->
            AllocationTransactionCard(tx = tx)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom FAB
        }
    }

    // Dialog: Manual Dispatch
    if (showManualDispatchDialog) {
        ManualDispatchDialog(
            depots = uiState.depots,
            onDismiss = { showManualDispatchDialog = false },
            onDispatch = { sourceId, targetZone, water, food, med, boats ->
                viewModel.dispatchManualAllocation(sourceId, targetZone, water, food, med, boats)
                showManualDispatchDialog = false
            }
        )
    }

    // Dialog: Emergency Restock
    restockTargetDepot?.let { depot ->
        EmergencyRestockDialog(
            depot = depot,
            onDismiss = { restockTargetDepot = null },
            onConfirmRestock = { water, food, med, boats, fuel ->
                viewModel.restockDepot(depot.id, water, food, med, boats, fuel)
                restockTargetDepot = null
            }
        )
    }
}

@Composable
private fun StockpileMetricChip(
    title: String,
    current: Int,
    capacity: Int,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    testTag: String
) {
    val emergencyColors = LocalEmergencyColors.current
    val progress = (current.toFloat() / capacity.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .width(180.dp)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = emergencyColors.textSecondary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(current),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = emergencyColors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/ %,d %s".format(capacity, unit),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = emergencyColors.textSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (progress < 0.25f) emergencyColors.redCritical else accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun DepotFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isWarning: Boolean = false,
    testTag: String
) {
    val emergencyColors = LocalEmergencyColors.current
    val bgColor = when {
        isSelected -> emergencyColors.blueLight
        isWarning -> emergencyColors.redCritical.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> Color.White
        isWarning -> emergencyColors.redCritical
        else -> emergencyColors.textSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                BorderStroke(
                    1.dp,
                    if (isWarning) emergencyColors.redCritical.copy(alpha = 0.5f) else emergencyColors.surfaceBorder
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ReliefDepotCard(
    depot: ReliefDepot,
    onRestockClick: () -> Unit,
    onDispatchClick: () -> Unit
) {
    val emergencyColors = LocalEmergencyColors.current

    val statusColor = when (depot.status) {
        DepotStatus.OPERATIONAL -> EmergencyGreenStable
        DepotStatus.DEPLETION_ALERT -> emergencyColors.orangeWarning
        DepotStatus.CRITICAL_SHORTAGE -> emergencyColors.redCritical
        DepotStatus.RESTOCK_IN_TRANSIT -> emergencyColors.blueLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("depot_card_${depot.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (depot.status == DepotStatus.CRITICAL_SHORTAGE) emergencyColors.redCritical.copy(alpha = 0.6f) else emergencyColors.surfaceBorder
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = depot.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.blueLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = depot.status.label.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = depot.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = emergencyColors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${depot.district} • ${depot.locationDescription} (${"%.4f".format(depot.lat)}, ${"%.4f".format(depot.lng)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            // Inventory Bars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DepotResourceBar(
                    label = "Potable Water",
                    current = depot.waterStock,
                    capacity = depot.waterCapacity,
                    unit = "Units",
                    color = emergencyColors.blueLight
                )
                DepotResourceBar(
                    label = "Ration Packs",
                    current = depot.rationPacks,
                    capacity = depot.rationCapacity,
                    unit = "Boxes",
                    color = emergencyColors.orangeBright
                )
                DepotResourceBar(
                    label = "Trauma Med-Kits",
                    current = depot.medicalTraumaKits,
                    capacity = depot.medicalCapacity,
                    unit = "Kits",
                    color = emergencyColors.magentaElectric
                )
                DepotResourceBar(
                    label = "Rescue Boats & Fuel",
                    current = depot.rescueBoats,
                    capacity = depot.boatCapacity,
                    unit = "Boats (${depot.emergencyFuelLiters}L Fuel)",
                    color = EmergencyGreenStable
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRestockClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = emergencyColors.blueLight),
                    border = BorderStroke(1.dp, emergencyColors.blueLight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_restock_${depot.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.AirplanemodeActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AIR-DROP RESTOCK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDispatchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.blueLight),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_dispatch_${depot.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DISPATCH CONVOY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DepotResourceBar(
    label: String,
    current: Int,
    capacity: Int,
    unit: String,
    color: Color
) {
    val emergencyColors = LocalEmergencyColors.current
    val progress = (current.toFloat() / capacity.coerceAtLeast(1)).coerceIn(0f, 1f)
    val isCritical = progress < 0.20f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = emergencyColors.textPrimary
            )
            Text(
                text = "%,d / %,d %s (%.0f%%)".format(current, capacity, unit, progress * 100),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCritical) emergencyColors.redCritical else emergencyColors.textSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isCritical) emergencyColors.redCritical else color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AllocationTransactionCard(tx: AllocationTransaction) {
    val emergencyColors = LocalEmergencyColors.current

    val statusColor = when (tx.status) {
        "DELIVERED" -> EmergencyGreenStable
        "IN_TRANSIT" -> emergencyColors.orangeBright
        else -> emergencyColors.blueLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_card_${tx.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tx.status) {
                        "DELIVERED" -> Icons.Filled.CheckCircle
                        "IN_TRANSIT" -> Icons.AutoMirrored.Filled.DirectionsRun
                        else -> Icons.AutoMirrored.Filled.Send
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tx.targetZoneName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tx.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "From: ${tx.sourceDepotName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Payload: ${tx.waterUnits} Water • ${tx.foodPackets} Rations • ${tx.medicalKits} MedKits • ${tx.rescueBoats} Zodiacs",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = emergencyColors.blueLight
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualDispatchDialog(
    depots: List<ReliefDepot>,
    onDismiss: () -> Unit,
    onDispatch: (sourceId: String, targetZone: String, water: Int, food: Int, med: Int, boats: Int) -> Unit
) {
    val emergencyColors = LocalEmergencyColors.current

    var selectedDepotIndex by remember { mutableIntStateOf(0) }
    var selectedZone by remember { mutableStateOf("Zone 1 - Riverbank Delta") }
    var waterAmount by remember { mutableFloatStateOf(150f) }
    var foodAmount by remember { mutableFloatStateOf(80f) }
    var medAmount by remember { mutableFloatStateOf(2f) }
    var boatsAmount by remember { mutableFloatStateOf(1f) }

    val activeDepot = depots.getOrNull(selectedDepotIndex) ?: return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .testTag("manual_dispatch_dialog")
            ) {
                Text(
                    text = "Manual Supply Dispatch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = emergencyColors.textPrimary
                )
                Text(
                    text = "Assign direct stockpile convoy to active incident zone",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Source Depot Selector
                Text(
                    text = "Source Relief Depot:",
                    style = MaterialTheme.typography.labelSmall,
                    color = emergencyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    depots.forEachIndexed { index, d ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedDepotIndex == index) emergencyColors.blueLight else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedDepotIndex = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = d.name.split("-").first().trim(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedDepotIndex == index) Color.White else emergencyColors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Zone Selector
                Text(
                    text = "Destination Disaster Zone:",
                    style = MaterialTheme.typography.labelSmall,
                    color = emergencyColors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val zones = listOf("Zone 1 - Riverbank Delta", "Zone 2 - Central Market Ward", "Zone 3 - Coastal Fishery Ward")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    zones.forEach { zone ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedZone == zone) emergencyColors.magentaTactical else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedZone = zone }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = zone.split("-").first().trim(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedZone == zone) Color.White else emergencyColors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sliders for quantities
                Text(
                    text = "Water Units: ${waterAmount.toInt()} (Max: ${activeDepot.waterStock})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.blueLight
                )
                Slider(
                    value = waterAmount,
                    onValueChange = { waterAmount = it },
                    valueRange = 0f..activeDepot.waterStock.toFloat().coerceAtLeast(1f),
                    steps = 10
                )

                Text(
                    text = "Ration Packs: ${foodAmount.toInt()} (Max: ${activeDepot.rationPacks})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.orangeBright
                )
                Slider(
                    value = foodAmount,
                    onValueChange = { foodAmount = it },
                    valueRange = 0f..activeDepot.rationPacks.toFloat().coerceAtLeast(1f),
                    steps = 10
                )

                Text(
                    text = "Medical Kits: ${medAmount.toInt()} | Rescue Boats: ${boatsAmount.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.magentaElectric
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Slider(
                        value = medAmount,
                        onValueChange = { medAmount = it },
                        valueRange = 0f..activeDepot.medicalTraumaKits.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.weight(1f)
                    )
                    Slider(
                        value = boatsAmount,
                        onValueChange = { boatsAmount = it },
                        valueRange = 0f..activeDepot.rescueBoats.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            onDispatch(
                                activeDepot.id,
                                selectedZone,
                                waterAmount.toInt(),
                                foodAmount.toInt(),
                                medAmount.toInt(),
                                boatsAmount.toInt()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.blueLight),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_confirm_manual_dispatch")
                    ) {
                        Text("CONFIRM DISPATCH", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyRestockDialog(
    depot: ReliefDepot,
    onDismiss: () -> Unit,
    onConfirmRestock: (water: Int, food: Int, med: Int, boats: Int, fuel: Int) -> Unit
) {
    val emergencyColors = LocalEmergencyColors.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .testTag("restock_dialog")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AirplanemodeActive,
                        contentDescription = null,
                        tint = emergencyColors.blueLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Air-Drop Restock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = emergencyColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Request immediate Indian Air Force / NDRF logistics sortie to replenish ${depot.name}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(emergencyColors.blueContainer)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "STANDARD AIR-DROP REPLENISHMENT PAYLOAD:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.blueLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• +500 Water Purification Units\n• +300 Ready-to-Eat Ration Packs\n• +25 Trauma First-Response Kits\n• +2 Zodiac Flood Extraction Inflatables\n• +1,000 Liters High-Octane Diesel",
                            style = MaterialTheme.typography.bodySmall,
                            color = emergencyColors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = { onConfirmRestock(500, 300, 25, 2, 1000) },
                        colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.blueLight),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_confirm_restock")
                    ) {
                        Text("TRANSMIT SORTIE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
