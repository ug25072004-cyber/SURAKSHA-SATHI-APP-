package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.EmergencyResourceEntity
import com.example.ui.theme.EmergencyGreenStable
import com.example.ui.theme.EmergencyMagentaAction
import com.example.ui.theme.EmergencyOrangeWarning
import com.example.ui.theme.EmergencyRedCritical
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyUiState
import com.example.viewmodel.EmergencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineDirectoryScreen(
    viewModel: EmergencyViewModel,
    uiState: EmergencyUiState,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isAddContactDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isAddResourceDialogOpen by rememberSaveable { mutableStateOf(false) }

    val filteredContacts = remember(
        uiState.offlineContacts,
        uiState.contactSearchQuery,
        uiState.selectedContactCategory
    ) {
        uiState.offlineContacts.filter { contact ->
            val matchesCategory = uiState.selectedContactCategory == "ALL" ||
                    contact.category.equals(uiState.selectedContactCategory, ignoreCase = true)
            val matchesQuery = uiState.contactSearchQuery.isBlank() ||
                    contact.name.contains(uiState.contactSearchQuery, ignoreCase = true) ||
                    contact.organization.contains(uiState.contactSearchQuery, ignoreCase = true) ||
                    contact.district.contains(uiState.contactSearchQuery, ignoreCase = true) ||
                    contact.primaryPhone.contains(uiState.contactSearchQuery)
            matchesCategory && matchesQuery
        }
    }

    val filteredResources = remember(
        uiState.offlineResources,
        uiState.resourceSearchQuery,
        uiState.selectedResourceCategory
    ) {
        uiState.offlineResources.filter { resource ->
            val matchesCategory = uiState.selectedResourceCategory == "ALL" ||
                    resource.category.equals(uiState.selectedResourceCategory, ignoreCase = true)
            val matchesQuery = uiState.resourceSearchQuery.isBlank() ||
                    resource.name.contains(uiState.resourceSearchQuery, ignoreCase = true) ||
                    resource.locationName.contains(uiState.resourceSearchQuery, ignoreCase = true) ||
                    resource.district.contains(uiState.resourceSearchQuery, ignoreCase = true) ||
                    resource.custodianName.contains(uiState.resourceSearchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("offline_directory_screen")) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Offline SQLite Room Cache Status Banner
            item {
                OfflineRoomBanner(
                    contactCount = uiState.offlineContacts.size,
                    resourceCount = uiState.offlineResources.size
                )
            }

            // Tabs: Emergency Contacts vs Critical Resources
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTab),
                            color = if (selectedTab == 0) EmergencyRedCritical else EmergencyMagentaAction
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("tab_contacts"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Emergency Contacts (${uiState.offlineContacts.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("tab_resources"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Critical Resources (${uiState.offlineResources.size})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Search and Filter Bar for Contacts
                item {
                    OutlinedTextField(
                        value = uiState.contactSearchQuery,
                        onValueChange = { viewModel.setContactSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth().testTag("input_search_contacts"),
                        placeholder = { Text("Search by name, dept, district, or phone...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search contacts")
                        },
                        trailingIcon = {
                            if (uiState.contactSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setContactSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = emergencyColors.surfaceBorder
                        )
                    )
                }

                // Category Chips for Contacts
                item {
                    val categories = listOf(
                        "ALL" to "All Hotlines",
                        "DISASTER_MGMT" to "Disaster Mgmt",
                        "MILITARY_NDRF" to "NDRF / Coast Guard",
                        "MEDICAL" to "Medical & EMS",
                        "POLICE" to "Police 112",
                        "FIRE_RESCUE" to "Fire & Rescue",
                        "DISTRICT_ADMIN" to "District Admin"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (catKey, catLabel) ->
                            FilterChip(
                                selected = uiState.selectedContactCategory == catKey,
                                onClick = { viewModel.setContactCategory(catKey) },
                                label = { Text(catLabel, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Contact Cards List
                if (filteredContacts.isEmpty()) {
                    item {
                        EmptyStateNotice(message = "No emergency contacts found matching filters.")
                    }
                } else {
                    items(filteredContacts, key = { it.id }) { contact ->
                        EmergencyContactCard(
                            contact = contact,
                            onDial = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            } else {
                // Search and Filter Bar for Resources
                item {
                    OutlinedTextField(
                        value = uiState.resourceSearchQuery,
                        onValueChange = { viewModel.setResourceSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth().testTag("input_search_resources"),
                        placeholder = { Text("Search by equipment name, location, district, custodian...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search resources")
                        },
                        trailingIcon = {
                            if (uiState.resourceSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setResourceSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = emergencyColors.surfaceBorder
                        )
                    )
                }

                // Category Chips for Resources
                item {
                    val resourceCategories = listOf(
                        "ALL" to "All Assets",
                        "HEAVY_MACHINERY" to "Dewatering Pumps",
                        "RESCUE_GEAR" to "Boats & Rafts",
                        "WATER_SANITATION" to "Water Purifiers",
                        "MEDICAL" to "Mobile ICUs",
                        "POWER_COMMUNICATION" to "Generators & Satellite",
                        "LIFE_SAVING" to "Food & Survival"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(resourceCategories) { (catKey, catLabel) ->
                            FilterChip(
                                selected = uiState.selectedResourceCategory == catKey,
                                onClick = { viewModel.setResourceCategory(catKey) },
                                label = { Text(catLabel, style = MaterialTheme.typography.labelMedium) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmergencyMagentaAction.copy(alpha = 0.2f),
                                    selectedLabelColor = EmergencyMagentaAction
                                )
                            )
                        }
                    }
                }

                // Critical Physical Resources List
                if (filteredResources.isEmpty()) {
                    item {
                        EmptyStateNotice(message = "No critical emergency resources found matching criteria.")
                    }
                } else {
                    items(filteredResources, key = { it.id }) { resource ->
                        EmergencyResourceCard(
                            resource = resource,
                            onContactCustodian = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to Add new offline contact or resource
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) isAddContactDialogOpen = true else isAddResourceDialogOpen = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("fab_add_offline_entry"),
            containerColor = if (selectedTab == 0) EmergencyRedCritical else EmergencyMagentaAction,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add offline entry")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedTab == 0) "Log Contact" else "Log Asset",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog: Add Contact
    if (isAddContactDialogOpen) {
        AddContactDialog(
            onDismiss = { isAddContactDialogOpen = false },
            onConfirm = { contact ->
                viewModel.addEmergencyContact(contact)
                isAddContactDialogOpen = false
            }
        )
    }

    // Dialog: Add Resource
    if (isAddResourceDialogOpen) {
        AddResourceDialog(
            onDismiss = { isAddResourceDialogOpen = false },
            onConfirm = { resource ->
                viewModel.addEmergencyResource(resource)
                isAddResourceDialogOpen = false
            }
        )
    }
}

@Composable
fun OfflineRoomBanner(
    contactCount: Int,
    resourceCount: Int,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_room_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(emergencyColors.surfaceBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EmergencyGreenStable.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OfflinePin,
                    contentDescription = null,
                    tint = EmergencyGreenStable,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ROOM OFFLINE DATABASE ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = EmergencyGreenStable
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$contactCount Emergency Contacts • $resourceCount Heavy Life-Saving Assets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = emergencyColors.textPrimary
                )
                Text(
                    text = "Encrypted SQLite Room persistence • Guaranteed zero-latency offline triage",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }
        }
    }
}

@Composable
fun EmergencyContactCard(
    contact: EmergencyContactEntity,
    onDial: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current

    val categoryColor = when (contact.category.uppercase()) {
        "MILITARY_NDRF" -> EmergencyMagentaAction
        "MEDICAL" -> EmergencyRedCritical
        "POLICE" -> MaterialTheme.colorScheme.primary
        "FIRE_RESCUE" -> EmergencyOrangeWarning
        else -> MaterialTheme.colorScheme.secondary
    }

    val categoryIcon = when (contact.category.uppercase()) {
        "MILITARY_NDRF" -> Icons.Default.Shield
        "MEDICAL" -> Icons.Default.LocalHospital
        "POLICE" -> Icons.Default.Security
        "FIRE_RESCUE" -> Icons.Default.Warning
        else -> Icons.Default.Business
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contact_card_${contact.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (contact.priorityRank == 1) categoryColor.copy(alpha = 0.5f) else emergencyColors.surfaceBorder
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = contact.organization,
                            style = MaterialTheme.typography.labelSmall,
                            color = emergencyColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.textPrimary
                        )
                    }
                }

                if (contact.isTollFree) {
                    Surface(
                        color = EmergencyGreenStable.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "TOLL-FREE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmergencyGreenStable
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = emergencyColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${contact.district} • ${contact.operationalHours}",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary
                )
            }

            contact.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: Call Primary & Alternate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onDial(contact.primaryPhone) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_dial_${contact.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = categoryColor
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Call ${contact.primaryPhone}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                contact.alternatePhone?.let { alt ->
                    if (alt.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onDial(alt) },
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("btn_dial_alt_${contact.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = emergencyColors.textPrimary
                            )
                        ) {
                            Text(text = "Alt: $alt", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyResourceCard(
    resource: EmergencyResourceEntity,
    onContactCustodian: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emergencyColors = LocalEmergencyColors.current

    val categoryColor = when (resource.category.uppercase()) {
        "HEAVY_MACHINERY" -> EmergencyOrangeWarning
        "RESCUE_GEAR" -> EmergencyMagentaAction
        "WATER_SANITATION" -> MaterialTheme.colorScheme.primary
        "MEDICAL" -> EmergencyRedCritical
        "POWER_COMMUNICATION" -> EmergencyGreenStable
        else -> MaterialTheme.colorScheme.secondary
    }

    val typeIcon = when (resource.category.uppercase()) {
        "HEAVY_MACHINERY" -> Icons.Default.Construction
        "RESCUE_GEAR" -> Icons.Default.LocalShipping
        "WATER_SANITATION" -> Icons.Default.WaterDrop
        "MEDICAL" -> Icons.Default.MedicalServices
        "POWER_COMMUNICATION" -> Icons.Default.ElectricBolt
        else -> Icons.Default.Handyman
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("resource_card_${resource.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (resource.isCritical) categoryColor.copy(alpha = 0.5f) else emergencyColors.surfaceBorder
            )
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = resource.category.replace('_', ' '),
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = resource.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.textPrimary
                        )
                    }
                }

                // Inventory badge
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(categoryColor.copy(alpha = 0.3f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${resource.availableQuantity} / ${resource.totalQuantity}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = categoryColor
                        )
                        Text(
                            text = "${resource.unit} Ready",
                            style = MaterialTheme.typography.labelSmall,
                            color = emergencyColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = emergencyColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${resource.locationName} (${resource.district})",
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            resource.description?.let { desc ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = emergencyColors.textPrimary.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custodian info & Dial action
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custodian In-Charge",
                            style = MaterialTheme.typography.labelSmall,
                            color = emergencyColors.textSecondary
                        )
                        Text(
                            text = resource.custodianName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.textPrimary
                        )
                    }
                    Button(
                        onClick = { onContactCustodian(resource.custodianPhone) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Custodian", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateNotice(message: String) {
    val emergencyColors = LocalEmergencyColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = emergencyColors.textSecondary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = emergencyColors.textSecondary
            )
        }
    }
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onConfirm: (EmergencyContactEntity) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var organization by rememberSaveable { mutableStateOf("") }
    var primaryPhone by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("Cuttack") }
    var category by rememberSaveable { mutableStateOf("DISASTER_MGMT") }
    var isTollFree by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("dialog_add_contact")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Offline Emergency Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name / Role") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Agency / Organization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = primaryPhone,
                    onValueChange = { primaryPhone = it },
                    label = { Text("Emergency Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District / Coverage") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && primaryPhone.isNotBlank()) {
                                onConfirm(
                                    EmergencyContactEntity(
                                        name = name,
                                        organization = if (organization.isBlank()) "Civil Defense" else organization,
                                        category = category,
                                        primaryPhone = primaryPhone,
                                        district = district,
                                        isTollFree = isTollFree
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedCritical)
                    ) {
                        Text("Save to Room DB")
                    }
                }
            }
        }
    }
}

@Composable
fun AddResourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (EmergencyResourceEntity) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("HEAVY_MACHINERY") }
    var quantity by rememberSaveable { mutableStateOf("10") }
    var unit by rememberSaveable { mutableStateOf("Units") }
    var locationName by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("Cuttack") }
    var custodianName by rememberSaveable { mutableStateOf("") }
    var custodianPhone by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("dialog_add_resource")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Log Offline Physical Resource",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Resource Name (e.g. 50HP Pumps)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Staging Location / Depot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = custodianName,
                    onValueChange = { custodianName = it },
                    label = { Text("Custodian Officer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = custodianPhone,
                    onValueChange = { custodianPhone = it },
                    label = { Text("Custodian Contact Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val qty = quantity.toIntOrNull() ?: 5
                            if (name.isNotBlank() && locationName.isNotBlank()) {
                                onConfirm(
                                    EmergencyResourceEntity(
                                        name = name,
                                        category = category,
                                        totalQuantity = qty,
                                        availableQuantity = qty,
                                        unit = unit,
                                        locationName = locationName,
                                        district = district,
                                        lat = 20.4625,
                                        lng = 85.8830,
                                        custodianName = if (custodianName.isBlank()) "Logistics Desk" else custodianName,
                                        custodianPhone = if (custodianPhone.isBlank()) "1077" else custodianPhone
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyMagentaAction)
                    ) {
                        Text("Save Asset to Room DB")
                    }
                }
            }
        }
    }
}
