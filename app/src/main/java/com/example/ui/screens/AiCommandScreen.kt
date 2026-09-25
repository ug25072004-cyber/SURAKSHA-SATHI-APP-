package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.LocalEmergencyColors
import com.example.viewmodel.EmergencyViewModel

@Composable
fun AiCommandScreen(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("ai_command_screen")
    ) {
        // AI Header with Intelligence Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            border = BorderStroke(1.dp, emergencyColors.surfaceBorder)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(emergencyColors.magentaTactical),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sathi Command AI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = if (uiState.isThinkingModeEnabled) "Model: gemini-3.1-pro-preview (Thinking: HIGH)" else "Model: gemini-3.5-flash (Fast)",
                                style = MaterialTheme.typography.labelSmall,
                                color = emergencyColors.magentaElectric
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Control Toggles: High Thinking Mode & Google Search Grounding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Thinking Mode Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(emergencyColors.surfaceBorder.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Psychology, contentDescription = null, tint = emergencyColors.magentaElectric, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "High Thinking", style = MaterialTheme.typography.labelSmall, color = emergencyColors.textPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = uiState.isThinkingModeEnabled,
                            onCheckedChange = { viewModel.toggleThinkingMode() },
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Search Grounding Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(emergencyColors.surfaceBorder.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = emergencyColors.blueLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Search Grounding", style = MaterialTheme.typography.labelSmall, color = emergencyColors.textPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = uiState.isSearchGroundingEnabled,
                            onCheckedChange = { viewModel.toggleSearchGrounding() },
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.chatMessages) { message ->
                ChatMessageItem(message = message, onActionClick = { action ->
                    viewModel.sendChatMessage(action)
                })
            }

            if (uiState.isAiThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = emergencyColors.magentaElectric,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.isThinkingModeEnabled) "SurakshaSathi AI is evaluating multi-factor disaster triage & VRP routing..." else "Generating situational response...",
                            style = MaterialTheme.typography.bodySmall,
                            color = emergencyColors.magentaElectric
                        )
                    }
                }
            }
        }

        // Quick Suggestion Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                listOf(
                    "Simulate bridge collapse & re-allocate",
                    "Zone 1 vs Zone 2 priority justification",
                    "Evacuation route safety analysis",
                    "Check emergency water reserves"
                )
            ) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(emergencyColors.bluePrimary.copy(alpha = 0.15f))
                        .border(1.dp, emergencyColors.blueLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.sendChatMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = emergencyColors.blueLight
                    )
                }
            }
        }

        // Input Dispatch Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_input_field"),
                placeholder = { Text("Ask SurakshaSathi Tactical AI...", style = MaterialTheme.typography.bodyMedium) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = emergencyColors.magentaElectric,
                    unfocusedBorderColor = emergencyColors.surfaceBorder
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(emergencyColors.magentaTactical)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send prompt",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onActionClick: (String) -> Unit
) {
    val emergencyColors = LocalEmergencyColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        // Thinking Trace Banner if enabled
        if (message.thinkingTrace != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(containerColor = emergencyColors.magentaContainer),
                border = BorderStroke(1.dp, emergencyColors.magentaElectric.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Psychology, contentDescription = null, tint = emergencyColors.magentaElectric, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "THINKING PROCESS (gemini-3.1-pro-preview)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = emergencyColors.magentaElectric
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.thinkingTrace,
                        style = MaterialTheme.typography.bodySmall,
                        color = emergencyColors.textPrimary
                    )
                }
            }
        }

        // Main Bubble
        Card(
            modifier = Modifier.fillMaxWidth(if (message.isUser) 0.8f else 0.94f),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) emergencyColors.bluePrimary else MaterialTheme.colorScheme.surface
            ),
            border = if (!message.isUser) BorderStroke(1.dp, emergencyColors.surfaceBorder) else null,
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (message.isUser) 14.dp else 2.dp,
                bottomEnd = if (message.isUser) 2.dp else 14.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) Color.White else emergencyColors.textPrimary
                )

                // Suggested action follow-ups
                if (message.suggestedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Suggested Directives:",
                        style = MaterialTheme.typography.labelSmall,
                        color = emergencyColors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    message.suggestedActions.forEach { action ->
                        Text(
                            text = "• $action",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onActionClick(action) }
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = emergencyColors.blueLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
