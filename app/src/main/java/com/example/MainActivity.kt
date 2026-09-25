package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.AuthState
import com.example.ui.components.AuthDialog
import com.example.ui.components.EmergencyProtocolDialog
import com.example.ui.components.SosEmergencyFab
import com.example.ui.components.SurakshaLogo
import com.example.ui.screens.AiCommandScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GisRoutingScreen
import com.example.ui.screens.IncidentReportScreen
import com.example.ui.screens.OfflineDirectoryScreen
import com.example.ui.screens.StockpileDashboardScreen
import com.example.ui.theme.EmergencyRedCritical
import com.example.ui.theme.EmergencyRedPulse
import com.example.ui.theme.LocalEmergencyColors
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EmergencyViewModel

enum class NavigationScreen(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Command", Icons.Filled.SpaceDashboard, "nav_dashboard"),
    STOCKPILE("Stockpile", Icons.Filled.LocalShipping, "nav_stockpile"),
    GIS("GIS Route", Icons.Filled.Route, "nav_gis"),
    AI("Sathi AI", Icons.Filled.SmartToy, "nav_ai"),
    DIRECTORY("Directory", Icons.Filled.ContactPhone, "nav_directory"),
    INCIDENTS("Registry", Icons.AutoMirrored.Filled.ListAlt, "nav_incidents")
}

class MainActivity : ComponentActivity() {
    private val viewModel: EmergencyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MyApplicationTheme(darkTheme = uiState.isDarkTheme) {
                SurakshaSathiApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurakshaSathiApp(
    viewModel: EmergencyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val emergencyColors = LocalEmergencyColors.current
    var currentScreen by rememberSaveable { mutableStateOf(NavigationScreen.DASHBOARD) }
    var isSosDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isAuthDialogOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            SosEmergencyFab(
                onClick = { isSosDialogOpen = true },
                isBroadcasting = uiState.isSosProtocolActive
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SurakshaLogo(size = 32.dp, showWordmark = false)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SurakshaSathi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = emergencyColors.textPrimary
                            )
                            Text(
                                text = "Crisis Core • SIH 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = emergencyColors.textSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Firebase Auth Profile / Sign-In Button
                    IconButton(
                        onClick = { isAuthDialogOpen = true },
                        modifier = Modifier.testTag("auth_profile_button")
                    ) {
                        val auth = uiState.authState
                        if (auth is AuthState.Authenticated) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(emergencyColors.bluePrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (auth.displayName?.take(1) ?: auth.email?.take(1) ?: "R").uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = emergencyColors.blueLight
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Firebase Crisis Sign-In",
                                tint = emergencyColors.blueLight
                            )
                        }
                    }

                    // Theme Switcher Button
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Dark/Light Mode",
                            tint = emergencyColors.orangeBright
                        )
                    }

                    // SOS Quick Action Beacon
                    IconButton(
                        onClick = { viewModel.triggerDynamicReallocation() },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(emergencyColors.redCritical.copy(alpha = 0.2f))
                            .testTag("sos_beacon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CrisisAlert,
                            contentDescription = "Trigger Emergency Triage Re-allocation",
                            tint = emergencyColors.redCritical,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationScreen.entries.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = emergencyColors.blueLight,
                            indicatorColor = when (screen) {
                                NavigationScreen.DASHBOARD -> emergencyColors.bluePrimary
                                NavigationScreen.STOCKPILE -> emergencyColors.magentaTactical
                                NavigationScreen.GIS -> emergencyColors.blueLight
                                NavigationScreen.AI -> emergencyColors.magentaTactical
                                NavigationScreen.DIRECTORY -> emergencyColors.greenStable
                                NavigationScreen.INCIDENTS -> emergencyColors.bluePrimary
                            },
                            unselectedIconColor = emergencyColors.textSecondary,
                            unselectedTextColor = emergencyColors.textSecondary
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Persistent SOS Broadcasting Alert Banner if active
                if (uiState.isSosProtocolActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmergencyRedCritical)
                            .clickable { isSosDialogOpen = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("active_sos_system_banner"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CrisisAlert,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SOS ACTIVE: ${uiState.activeSosProtocolName ?: "EMERGENCY BEACON"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "MANAGE / ABORT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            NavigationScreen.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToGis = { currentScreen = NavigationScreen.GIS },
                                onNavigateToAi = { currentScreen = NavigationScreen.AI },
                                onNavigateToStockpile = { currentScreen = NavigationScreen.STOCKPILE },
                                onNavigateToDirectory = { currentScreen = NavigationScreen.DIRECTORY }
                            )
                            NavigationScreen.STOCKPILE -> StockpileDashboardScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.GIS -> GisRoutingScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.AI -> AiCommandScreen(
                                viewModel = viewModel
                            )
                            NavigationScreen.DIRECTORY -> OfflineDirectoryScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                            NavigationScreen.INCIDENTS -> IncidentReportScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // Emergency Protocol Dialog Flow
            EmergencyProtocolDialog(
                isOpen = isSosDialogOpen,
                onDismiss = { isSosDialogOpen = false },
                onExecuteProtocol = { protocolName, details ->
                    viewModel.executeSosProtocol(protocolName, details)
                    isSosDialogOpen = false
                },
                isAlreadyActive = uiState.isSosProtocolActive,
                activeProtocolName = uiState.activeSosProtocolName,
                onCancelActiveSos = {
                    viewModel.dismissSosProtocol()
                }
            )

            // Firebase Authentication Dialog Flow
            AuthDialog(
                isOpen = isAuthDialogOpen,
                onDismiss = { isAuthDialogOpen = false },
                viewModel = viewModel
            )
        }
    }
}

/**
 * Kept for testing compatibility with GreetingScreenshotTest.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
