package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.firebase.StorageFileItem
import com.example.data.firebase.StorageUploadState
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudStorageVaultDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    val context = LocalContext.current

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadIncidentEvidence(context, uri, folder = "incidents/evidence")
        }
    }

    // Generic Document/File Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadIncidentEvidence(context, uri, folder = "incidents/documents")
        }
    }

    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .height(620.dp)
                .padding(vertical = 16.dp)
                .testTag("storage_vault_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, emergencyColors.blueLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
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
                                .background(emergencyColors.bluePrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudQueue,
                                contentDescription = null,
                                tint = emergencyColors.blueLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FIREBASE EVIDENCE VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = "suraksha-sathi-app-ebb0b.firebasestorage.app",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = emergencyColors.blueLight
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { viewModel.loadStorageFiles() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh files",
                                tint = emergencyColors.textSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Text("✕", style = MaterialTheme.typography.titleMedium, color = emergencyColors.textSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.authState !is com.example.data.firebase.AuthState.Authenticated) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = emergencyColors.bluePrimary.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, emergencyColors.blueLight.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "ℹ️ Bucket in Production Mode: If security rules require auth (request.auth != null), sign in before uploading.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = emergencyColors.blueLight,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Upload Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vault_btn_upload_photo"),
                        colors = ButtonDefaults.buttonColors(containerColor = emergencyColors.bluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Photo", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { documentPickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vault_btn_upload_doc"),
                        border = BorderStroke(1.dp, emergencyColors.bluePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = emergencyColors.blueLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload File", style = MaterialTheme.typography.labelMedium, color = emergencyColors.blueLight)
                    }
                }

                // Live Uploading Progress Card
                when (val uploadState = uiState.storageUploadState) {
                    is StorageUploadState.Uploading -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = emergencyColors.bluePrimary.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, emergencyColors.blueLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Uploading: ${uploadState.fileName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = emergencyColors.blueLight
                                    )
                                    Text(
                                        text = "${(uploadState.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = emergencyColors.blueLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { uploadState.progress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = emergencyColors.blueLight
                                )
                            }
                        }
                    }
                    is StorageUploadState.Success -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = emergencyColors.greenStable.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, emergencyColors.greenStable)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.CloudDone, contentDescription = null, tint = emergencyColors.greenStable, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Uploaded: ${uploadState.result.fileName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = emergencyColors.greenStable
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        copyToClipboard(context, uploadState.result.downloadUrl)
                                        Toast.makeText(context, "Cloud URL copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy URL", tint = emergencyColors.greenStable, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    is StorageUploadState.Error -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = emergencyColors.redContainer),
                            border = BorderStroke(1.dp, emergencyColors.redCritical)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = emergencyColors.redCritical, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uploadState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = emergencyColors.redCritical
                                )
                            }
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = emergencyColors.surfaceBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // File List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLOUD STORED ASSETS (${uiState.storageFiles.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emergencyColors.textSecondary,
                        letterSpacing = 1.sp
                    )

                    if (uiState.isLoadingStorageFiles) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = emergencyColors.blueLight)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (uiState.storageFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = null,
                                tint = emergencyColors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No evidence files uploaded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = emergencyColors.textSecondary
                            )
                            Text(
                                text = "Upload field damage photos to test Firebase Storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = emergencyColors.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.storageFiles) { fileItem ->
                            StorageFileRow(
                                file = fileItem,
                                onCopyUrl = {
                                    copyToClipboard(context, fileItem.downloadUrl)
                                    Toast.makeText(context, "Download link copied", Toast.LENGTH_SHORT).show()
                                },
                                onOpenUrl = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileItem.downloadUrl))
                                    context.startActivity(intent)
                                },
                                onDelete = {
                                    viewModel.deleteStorageFile(fileItem.path)
                                    Toast.makeText(context, "Deleting file from Firebase...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageFileRow(
    file: StorageFileItem,
    onCopyUrl: () -> Unit,
    onOpenUrl: () -> Unit,
    onDelete: () -> Unit
) {
    val emergencyColors = LocalEmergencyColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, emergencyColors.surfaceBorder),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or Document Icon
            if (file.contentType.startsWith("image/")) {
                AsyncImage(
                    model = file.downloadUrl,
                    contentDescription = file.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.DarkGray)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(emergencyColors.bluePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = null,
                        tint = emergencyColors.blueLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${file.readableSize} • Firebase Storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }

            // Quick Actions
            IconButton(onClick = onCopyUrl, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy URL", tint = emergencyColors.blueLight, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onOpenUrl, modifier = Modifier.size(30.dp)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in browser", tint = emergencyColors.textSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete from storage", tint = emergencyColors.redCritical, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Firebase Storage Download Link", text)
    clipboard.setPrimaryClip(clip)
}
