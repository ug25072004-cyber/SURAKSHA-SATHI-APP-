package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.firebase.StorageUploadState
import com.example.ui.components.CloudStorageVaultDialog
import com.example.ui.components.EmergencyActionButton
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentReportScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val incidents by viewModel.incidentsFlow.collectAsState()
    val resources by viewModel.resourcesFlow.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var disasterType by remember { mutableStateOf("Flash Flood") }
    var severity by remember { mutableStateOf("High") }
    var affectedPeopleStr by remember { mutableStateOf("500") }
    var description by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf(false) }

    var isStorageVaultOpen by remember { mutableStateOf(false) }
    var selectedFullPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker for field evidence
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setSelectedEvidenceUri(uri)
            viewModel.uploadIncidentEvidence(context, uri, folder = "incidents/evidence")
            Toast.makeText(context, "Uploading evidence to Firebase Storage...", Toast.LENGTH_SHORT).show()
        }
    }

    val dateFormat = remember { SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("incident_report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OFFLINE & CLOUD REGISTRY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.blueLight,
                        letterSpacing = 1.sp
                    )

                    // Cloud Storage Vault Launcher Button
                    OutlinedButton(
                        onClick = { isStorageVaultOpen = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, emergencyColors.blueLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_open_storage_vault")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = emergencyColors.blueLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Storage Vault",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.blueLight
                        )
                    }
                }

                Text(
                    text = "Incident Registry & Cloud Evidence",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary
                )
                Text(
                    text = "Synchronized with Firebase Storage (${viewModel.storageService.defaultBucket}) and Local Room DB.",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }
        }

        // New Incident Report Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, emergencyColors.orangeBright.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILE FIELD INCIDENT REPORT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.orangeBright
                        )
                        Icon(imageVector = Icons.Filled.ReportProblem, contentDescription = null, tint = emergencyColors.orangeBright)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Incident Title (e.g. Submerged Culvert)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_incident_title")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Landmark") },
                            modifier = Modifier.weight(1f).testTag("input_incident_location")
                        )
                        OutlinedTextField(
                            value = affectedPeopleStr,
                            onValueChange = { affectedPeopleStr = it },
                            label = { Text("People Affected") },
                            modifier = Modifier.weight(1f).testTag("input_incident_people")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Operational Details / Demands") },
                        modifier = Modifier.fillMaxWidth().testTag("input_incident_desc"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ==========================================
                    // FIREBASE STORAGE PHOTO EVIDENCE ATTACHMENT
                    // ==========================================
                    Text(
                        text = "FIELD DAMAGE EVIDENCE (FIREBASE STORAGE)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (uiState.selectedEvidenceUri == null && uiState.uploadedEvidenceUrl == null) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_attach_evidence_photo"),
                            border = BorderStroke(1.dp, emergencyColors.bluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Image, contentDescription = null, tint = emergencyColors.blueLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Attach Field Photo / Evidence", color = emergencyColors.blueLight)
                        }
                    } else {
                        // Evidence Preview & Upload Status Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = uiState.uploadedEvidenceUrl ?: uiState.selectedEvidenceUri,
                                        contentDescription = "Attached damage evidence",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        if (uiState.uploadedEvidenceUrl != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.CloudDone,
                                                    contentDescription = null,
                                                    tint = emergencyColors.greenStable,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Uploaded to Firebase Storage",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = emergencyColors.greenStable
                                                )
                                            }
                                            Text(
                                                text = "Direct URL Linked to Incident",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = emergencyColors.textSecondary
                                            )
                                        } else {
                                            when (val state = uiState.storageUploadState) {
                                                is StorageUploadState.Uploading -> {
                                                    Text(
                                                        text = "Uploading to Cloud: ${(state.progress * 100).toInt()}%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = emergencyColors.blueLight
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    LinearProgressIndicator(
                                                        progress = { state.progress },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        color = emergencyColors.blueLight
                                                    )
                                                }
                                                is StorageUploadState.Error -> {
                                                    Text(
                                                        text = state.message,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = emergencyColors.redCritical
                                                    )
                                                }
                                                else -> {
                                                    Text(
                                                        text = "Ready to upload",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = emergencyColors.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.resetStorageUploadState() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove photo",
                                            tint = emergencyColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    EmergencyActionButton(
                        text = "SAVE REPORT & EVIDENCE TO REGISTRY",
                        icon = Icons.Filled.Add,
                        onClick = {
                            if (title.isNotBlank() && location.isNotBlank()) {
                                viewModel.submitNewIncident(
                                    title = title,
                                    disasterType = disasterType,
                                    location = location,
                                    severity = severity,
                                    affectedCount = affectedPeopleStr.toIntOrNull() ?: 100,
                                    description = description,
                                    photoUrl = uiState.uploadedEvidenceUrl
                                )
                                title = ""
                                location = ""
                                description = ""
                                successMsg = true
                            }
                        },
                        containerColor = emergencyColors.orangeWarning,
                        testTag = "button_save_incident"
                    )

                    if (successMsg) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Report & cloud evidence saved to registry.", color = Color(0xFF00E676), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Local Depot Stockpile Summary
        item {
            Text(
                text = "REGIONAL DEPOT STOCKPILES",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = emergencyColors.textSecondary,
                letterSpacing = 1.sp
            )
        }

        items(resources) { depot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = depot.depotName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = depot.district,
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                        Icon(imageVector = Icons.Filled.Inventory, contentDescription = null, tint = emergencyColors.blueLight)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Water: ${depot.waterUnits} units", style = MaterialTheme.typography.bodySmall, color = emergencyColors.blueLight)
                        Text(text = "Rations: ${depot.foodPackets} pkts", style = MaterialTheme.typography.bodySmall, color = emergencyColors.orangeWarning)
                        Text(text = "Boats: ${depot.rescueBoats}", style = MaterialTheme.typography.bodySmall, color = emergencyColors.magentaElectric)
                        Text(text = "Medical: ${depot.medicalKits}", style = MaterialTheme.typography.bodySmall, color = emergencyColors.redCritical)
                    }
                }
            }
        }

        // Saved Incident Reports List
        item {
            Text(
                text = "LOGGED INCIDENT FEED (${incidents.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = emergencyColors.textSecondary,
                letterSpacing = 1.sp
            )
        }

        items(incidents) { incident ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = incident.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(emergencyColors.redCritical.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = incident.severity,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.redCritical
                            )
                        }
                    }

                    Text(
                        text = "${incident.location} • ${incident.disasterType} • ${incident.affectedCount} Affected",
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = incident.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = emergencyColors.textPrimary
                    )

                    // Photo Evidence attached from Firebase Storage
                    if (!incident.photoUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .clickable { selectedFullPhotoUrl = incident.photoUrl }
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = incident.photoUrl,
                                contentDescription = "Incident damage photo",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudDone,
                                        contentDescription = null,
                                        tint = emergencyColors.greenStable,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Cloud Evidence Attached",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = emergencyColors.greenStable
                                    )
                                }
                                Text(
                                    text = "Tap to expand high-res field photo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = emergencyColors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reported by ${incident.reportedBy} • ${dateFormat.format(Date(incident.timestamp))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = emergencyColors.textSecondary
                    )
                }
            }
        }
    }

    // Storage Vault Dialog
    CloudStorageVaultDialog(
        isOpen = isStorageVaultOpen,
        onDismiss = { isStorageVaultOpen = false },
        viewModel = viewModel
    )

    // Full Screen Photo View Modal
    if (selectedFullPhotoUrl != null) {
        Dialog(onDismissRequest = { selectedFullPhotoUrl = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FIREBASE EVIDENCE VIEWER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.blueLight
                        )
                        IconButton(onClick = { selectedFullPhotoUrl = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = selectedFullPhotoUrl,
                        contentDescription = "Full damage evidence photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
