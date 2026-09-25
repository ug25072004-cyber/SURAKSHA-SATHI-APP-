package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DisasterLiveEpicenter
import com.example.data.model.EmergencyConvoyRoute
import com.example.data.model.GISRouteWaypoint
import com.example.data.model.MapDisplayMode
import com.example.ui.components.EmergencyActionButton
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel
import java.util.Locale

@Composable
fun GisRoutingScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    val context = LocalContext.current
    val route = uiState.activeRoute
    val epicenter = uiState.liveDisasterEpicenter
    val liveGps = uiState.liveGps

    // Runtime Permission Launcher for Real Device GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startLiveLocationTracking()
            Toast.makeText(context, "Live GPS Tracking Engaged (RTK Fix)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission required for live device tracking", Toast.LENGTH_LONG).show()
        }
    }

    // Radar pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("gis_routing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header & Operational Status
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GIS LIVE DISASTER DASHBOARD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.blueLight,
                        letterSpacing = 1.sp
                    )

                    // Live Telemetry Sync Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (uiState.isRealTimeTelemetryStreaming) Color(0xFF00E676).copy(alpha = 0.15f)
                                else Color.Gray.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.isRealTimeTelemetryStreaming) Color(0xFF00E676) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isRealTimeTelemetryStreaming) "LIVE IOT STREAM" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isRealTimeTelemetryStreaming) Color(0xFF00E676) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tactical Spatial Routing & Live Geo-Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary
                )
                Text(
                    text = "Real-time Google Maps telemetry overlay, dynamic evacuation corridors, and precision GPS responder tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }
        }

        // Map Display Layer Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapDisplayMode.entries.forEach { mode ->
                    val isSelected = uiState.mapDisplayMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setMapDisplayMode(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (mode) {
                                    MapDisplayMode.GOOGLE_MAPS_GIS -> Icons.Filled.LocationOn
                                    MapDisplayMode.TACTICAL_RADAR -> Icons.Filled.Sensors
                                    MapDisplayMode.SATELLITE_HAZARD -> Icons.Filled.Layers
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = emergencyColors.bluePrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("map_mode_${mode.name}")
                    )
                }
            }
        }

        // Interactive Live Tactical / Google Maps Vector Canvas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("gis_map_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070F1E)),
                border = BorderStroke(1.5.dp, emergencyColors.bluePrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Custom GIS Vector Map Drawing
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Base Spatial Grid
                        val gridColor = Color(0xFF1E3A5F).copy(alpha = 0.35f)
                        for (x in 0 until w.toInt() step 55) {
                            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), 1f)
                        }
                        for (y in 0 until h.toInt() step 55) {
                            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), 1f)
                        }

                        // Layer Specific Rendering
                        when (uiState.mapDisplayMode) {
                            MapDisplayMode.SATELLITE_HAZARD -> {
                                // Flood Hazard Inundation Polygons (Gradient Shaded)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFD32F2F).copy(alpha = 0.45f),
                                            Color(0xFFFF9800).copy(alpha = 0.25f),
                                            Color.Transparent
                                        ),
                                        center = Offset(w * 0.72f, h * 0.48f),
                                        radius = 160f
                                    ),
                                    radius = 160f,
                                    center = Offset(w * 0.72f, h * 0.48f)
                                )
                            }
                            MapDisplayMode.TACTICAL_RADAR -> {
                                // Radar range rings (5km, 10km, 15km range arcs)
                                drawCircle(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                                    radius = 70f,
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    style = Stroke(width = 1.5f)
                                )
                                drawCircle(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                                    radius = 140f,
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                            MapDisplayMode.GOOGLE_MAPS_GIS -> {
                                // Google Maps Style Vector Street / River Arteries
                                drawLine(
                                    color = Color(0xFF0288D1).copy(alpha = 0.4f),
                                    start = Offset(0f, h * 0.85f),
                                    end = Offset(w, h * 0.35f),
                                    strokeWidth = 14f
                                )
                                drawLine(
                                    color = Color(0xFF37474F).copy(alpha = 0.7f),
                                    start = Offset(w * 0.1f, 0f),
                                    end = Offset(w * 0.85f, h),
                                    strokeWidth = 6f
                                )
                            }
                        }

                        // Blocked Submerged Route (Red Dashed Line)
                        val blockedPath = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        drawLine(
                            color = Color(0xFFD32F2F),
                            start = Offset(w * 0.22f, h * 0.75f),
                            end = Offset(w * 0.62f, h * 0.62f),
                            strokeWidth = 4f,
                            pathEffect = blockedPath
                        )

                        // Active Safe Convoy Corridor (Cyan / Magenta)
                        val activeLineColor = if (route?.isRerouted == true) Color(0xFFE91E63) else Color(0xFF00E5FF)
                        drawLine(
                            color = activeLineColor,
                            start = Offset(w * 0.22f, h * 0.75f),
                            end = Offset(w * 0.42f, h * 0.28f),
                            strokeWidth = 5f
                        )
                        drawLine(
                            color = activeLineColor,
                            start = Offset(w * 0.42f, h * 0.28f),
                            end = Offset(w * 0.72f, h * 0.48f),
                            strokeWidth = 5f
                        )

                        // --- DISASTER EPICENTER (Live Pulsing Hazard Marker) ---
                        val epicenterCenter = Offset(w * 0.72f, h * 0.48f)
                        // Outer pulsating ring
                        drawCircle(
                            color = Color(0xFFFF1744).copy(alpha = pulseAlpha),
                            radius = pulseRadius,
                            center = epicenterCenter,
                            style = Stroke(width = 3f)
                        )
                        // Inner hazard beacon
                        drawCircle(Color(0xFFD50000), radius = 16f, center = epicenterCenter)
                        drawCircle(Color.White, radius = 7f, center = epicenterCenter)

                        // --- RESPONDER / CONVOY LIVE GPS POSITION ---
                        val responderCenter = Offset(w * 0.22f, h * 0.75f)
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                            radius = 24f,
                            center = responderCenter
                        )
                        drawCircle(Color(0xFF0D47A1), radius = 14f, center = responderCenter)
                        drawCircle(Color(0xFF00E676), radius = 6f, center = responderCenter)

                        // Waypoint 2: Highland Ridge Bypass
                        drawCircle(Color(0xFF00E5FF), radius = 9f, center = Offset(w * 0.42f, h * 0.28f))

                        // Submerged Hazard Node
                        drawCircle(Color(0xFFD32F2F), radius = 11f, center = Offset(w * 0.62f, h * 0.62f))
                    }

                    // Tactical HUD Overlays (Top Bar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EPICENTER: ${epicenter.latitude.formatCoord()}°N, ${epicenter.longitude.formatCoord()}°E",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFF5252)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (uiState.isLiveTrackingActive) "GPS LOCKED (±${liveGps?.accuracyMeters?.toInt() ?: 3}m)" else "SIMULATED TELEMETRY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isLiveTrackingActive) Color(0xFF00E676) else emergencyColors.blueLight
                            )
                        }
                    }

                    // Bottom Floating Action Overlay inside Map: Direct Google Maps Navigation
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Button(
                            onClick = {
                                openInGoogleMaps(
                                    context = context,
                                    lat = epicenter.latitude,
                                    lng = epicenter.longitude,
                                    label = epicenter.name
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A73E8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("launch_google_maps_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Maps Live",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Live Disaster Epicenter Telemetry Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("epicenter_telemetry_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, emergencyColors.redCritical.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(emergencyColors.redCritical)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = epicenter.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.textPrimary
                                )
                            }
                            Text(
                                text = "Coordinates: ${epicenter.latitude}° N, ${epicenter.longitude}° E | Radius: ${epicenter.radiusKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = emergencyColors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(emergencyColors.redContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = epicenter.severityLevel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.redCritical
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = emergencyColors.surfaceBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live IoT Sensor Telemetry Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GisTelemetryStat(
                            label = "Flood Inundation",
                            value = String.format(Locale.US, "%.2f m", epicenter.waterLevelMeters),
                            accentColor = Color(0xFF00E5FF)
                        )
                        GisTelemetryStat(
                            label = "Wind Velocity",
                            value = String.format(Locale.US, "%.0f km/h", epicenter.windSpeedKmh),
                            accentColor = emergencyColors.orangeWarning
                        )
                        GisTelemetryStat(
                            label = "At-Risk Civilians",
                            value = "${epicenter.affectedPopulationEstimate}",
                            accentColor = emergencyColors.redCritical
                        )
                        GisTelemetryStat(
                            label = "Active Hotspots",
                            value = "${epicenter.activeHotspotsCount} Nodes",
                            accentColor = emergencyColors.magentaElectric
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Row: Google Maps Direct Launch & Navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                openInGoogleMaps(
                                    context = context,
                                    lat = epicenter.latitude,
                                    lng = epicenter.longitude,
                                    label = epicenter.name
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_maps_overview_btn"),
                            border = BorderStroke(1.dp, emergencyColors.bluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = emergencyColors.blueLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "View Epicenter",
                                style = MaterialTheme.typography.labelMedium,
                                color = emergencyColors.blueLight
                            )
                        }

                        Button(
                            onClick = {
                                startGoogleMapsTurnByTurn(
                                    context = context,
                                    lat = epicenter.latitude,
                                    lng = epicenter.longitude
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_turn_by_turn_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.bluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tactical Navigation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Live Responder / Convoy Positioning (Fused Location Provider)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("responder_gps_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
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
                                imageVector = Icons.Filled.MyLocation,
                                contentDescription = null,
                                tint = if (uiState.isLiveTrackingActive) Color(0xFF00E676) else emergencyColors.blueLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RESPONDER GPS TELEMETRY",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                        }

                        // Live GPS Toggle Button
                        Button(
                            onClick = {
                                if (uiState.isLiveTrackingActive) {
                                    viewModel.stopLiveLocationTracking()
                                    Toast.makeText(context, "Live device tracking stopped", Toast.LENGTH_SHORT).show()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isLiveTrackingActive) emergencyColors.redCritical
                                else emergencyColors.bluePrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("toggle_device_gps_btn")
                        ) {
                            Text(
                                text = if (uiState.isLiveTrackingActive) "Stop Device GPS" else "Engage Device GPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (liveGps != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GisTelemetryStat(
                                label = "Convoy Speed",
                                value = String.format(Locale.US, "%.1f km/h", liveGps.speedKmh),
                                accentColor = emergencyColors.textPrimary
                            )
                            GisTelemetryStat(
                                label = "Dist to Epicenter",
                                value = String.format(Locale.US, "%.1f km", uiState.distanceToDisasterKm ?: 4.2),
                                accentColor = emergencyColors.blueLight
                            )
                            GisTelemetryStat(
                                label = "Bearing",
                                value = String.format(Locale.US, "%.0f°", uiState.bearingToDisasterDegrees ?: 145f),
                                accentColor = emergencyColors.orangeWarning
                            )
                            GisTelemetryStat(
                                label = "Accuracy",
                                value = String.format(Locale.US, "±%.1fm", liveGps.accuracyMeters),
                                accentColor = Color(0xFF00E676)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Source: ${liveGps?.provider ?: "FusedLocationProviderClient"} | Updated: Real-Time Stream",
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textSecondary
                    )
                }
            }
        }

        // Active Convoy Route Telemetry Card
        if (route != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = route.routeName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.textPrimary
                                )
                                Text(
                                    text = "${route.origin} -> ${route.primaryTarget}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = emergencyColors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(emergencyColors.bluePrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SAFETY: ${route.safetyScorePercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.blueLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RouteStat(label = "Total Distance", value = "${route.totalDistanceKm} km")
                            RouteStat(label = "Est. Transit Time", value = "${route.estimatedMinutes} mins")
                            RouteStat(label = "Waypoints", value = "${route.waypoints.size}")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Hazard Warning Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(emergencyColors.orangeContainer)
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = emergencyColors.orangeWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = route.hazardAdvisory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = emergencyColors.orangeWarning,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Step-by-Step Waypoint Guidance
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TURN-BY-TURN CORRIDOR NODES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textSecondary,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = { viewModel.toggleTelemetryStreaming() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh telemetry",
                            tint = emergencyColors.blueLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            itemsIndexed(route.waypoints) { index, waypoint ->
                val isSelected = uiState.selectedWaypoint?.id == waypoint.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectWaypoint(if (isSelected) null else waypoint) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) emergencyColors.bluePrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isSelected -> emergencyColors.blueLight
                            waypoint.isHazard -> emergencyColors.redCritical
                            else -> emergencyColors.surfaceBorder
                        }
                    ),
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
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        waypoint.isHazard -> emergencyColors.redCritical
                                        waypoint.isDepot -> emergencyColors.bluePrimary
                                        waypoint.isDestination -> emergencyColors.magentaElectric
                                        else -> emergencyColors.blueLight
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = waypoint.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (waypoint.isHazard) emergencyColors.redCritical else emergencyColors.textPrimary
                            )
                            Text(
                                text = "${waypoint.statusText} (${waypoint.lat.formatCoord()}°N, ${waypoint.lng.formatCoord()}°E)",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                openInGoogleMaps(
                                    context = context,
                                    lat = waypoint.lat,
                                    lng = waypoint.lng,
                                    label = waypoint.name
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open waypoint in Google Maps",
                                tint = emergencyColors.blueLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Dynamic Reroute Button
            item {
                EmergencyActionButton(
                    text = if (route.isRerouted) "REVERT TO PRIMARY CORRIDOR" else "TRIGGER DYNAMIC REROUTE (VRP)",
                    icon = Icons.AutoMirrored.Filled.AltRoute,
                    onClick = {
                        if (route.isRerouted) {
                            viewModel.resetAllocationToBaseline()
                        } else {
                            viewModel.triggerDynamicReallocation()
                        }
                    },
                    containerColor = if (route.isRerouted) emergencyColors.blueLight else emergencyColors.magentaTactical,
                    testTag = "gis_reroute_toggle_button"
                )
            }
        }
    }
}

@Composable
private fun GisTelemetryStat(
    label: String,
    value: String,
    accentColor: Color
) {
    val emergencyColors = LocalEmergencyColors.current
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = emergencyColors.textSecondary,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun RouteStat(label: String, value: String) {
    val emergencyColors = LocalEmergencyColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = emergencyColors.textSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = emergencyColors.textPrimary
        )
    }
}

private fun Double.formatCoord(): String = String.format(Locale.US, "%.4f", this)

/**
 * Opens Google Maps app or web browser at the specific latitude, longitude, and label
 */
private fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String) {
    val uriStr = "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})"
    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to universal web link
            val browserUri = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)))
        }
    } catch (_: Exception) {
        val browserUri = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)))
    }
}

/**
 * Launches Turn-by-Turn GPS Navigation in Google Maps
 */
private fun startGoogleMapsTurnByTurn(context: Context, lat: Double, lng: Double) {
    val navUri = "google.navigation:q=$lat,$lng&mode=d"
    val navIntent = Intent(Intent.ACTION_VIEW, Uri.parse(navUri)).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        if (navIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(navIntent)
        } else {
            val browserUri = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)))
        }
    } catch (_: Exception) {
        val browserUri = "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)))
    }
}
