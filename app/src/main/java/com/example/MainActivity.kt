package com.example

import android.Manifest
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.model.VoiceNavAlert
import com.example.ui.components.AdaptiveRerouteDialog
import com.example.ui.components.EditContactDialog
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.LocationCrossCheckDialog
import com.example.ui.components.LocationPermissionDialog
import com.example.ui.components.ReportHazardDialog
import com.example.ui.components.VoiceCommandModal
import com.example.ui.screens.AiConsultScreen
import com.example.ui.screens.HazardsMapScreen
import com.example.ui.screens.NavigationHudScreen
import com.example.ui.screens.RoutePlannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderDarkBg
import com.example.ui.theme.SafeRiderRed
import com.example.ui.theme.SafeRiderTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.SafeRiderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            SafeRiderTheme {
                SafeRiderApp()
            }
        }
    }
}

@Composable
fun SafeRiderApp(
    viewModel: SafeRiderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isVoiceModalOpen by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        viewModel.onLocationPermissionResult(granted = granted, isFine = fineGranted)
    }

    val requestLocationPermission = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Handle back button behavior for sub-screens
    BackHandler(enabled = state.currentScreen != AppScreen.ROUTE_PLANNER && !state.isNavigating) {
        viewModel.setScreen(AppScreen.ROUTE_PLANNER)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SafeRiderDarkBg,
        bottomBar = {
            // High-contrast, sleek navigation bar respecting navigationBar insets
            NavigationBar(
                containerColor = Color(0xFF0C1322),
                contentColor = Color.White,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(width = 1.dp, color = Color(0xFF1B273A))
                    .testTag("safe_rider_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.ROUTE_PLANNER,
                    onClick = { viewModel.setScreen(AppScreen.ROUTE_PLANNER) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.ROUTE_PLANNER) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "Rute"
                        )
                    },
                    label = { Text("Rute", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = SafeRiderCyan,
                        indicatorColor = SafeRiderCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_route")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.NAVIGATION_HUD,
                    onClick = { viewModel.setScreen(AppScreen.NAVIGATION_HUD) },
                    icon = {
                        Box {
                            Icon(
                                imageVector = if (state.currentScreen == AppScreen.NAVIGATION_HUD) Icons.Filled.Navigation else Icons.Outlined.Navigation,
                                contentDescription = "HUD"
                            )
                            if (state.isNavigating) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SafeRiderCyan)
                                )
                            }
                        }
                    },
                    label = { Text("HUD", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = SafeRiderCyan,
                        indicatorColor = SafeRiderCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_hud")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.HAZARDS_MAP,
                    onClick = { viewModel.setScreen(AppScreen.HAZARDS_MAP) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.HAZARDS_MAP) Icons.Filled.ReportProblem else Icons.Outlined.ReportProblem,
                            contentDescription = "Bahaya"
                        )
                    },
                    label = { Text("Bahaya", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = SafeRiderCyan,
                        indicatorColor = SafeRiderCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_hazards")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.AI_ASSISTANT,
                    onClick = { viewModel.setScreen(AppScreen.AI_ASSISTANT) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.AI_ASSISTANT) Icons.Filled.Shield else Icons.Outlined.AutoAwesome,
                            contentDescription = "SafeRider AI"
                        )
                    },
                    label = { Text("AI Asisten", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = SafeRiderCyan,
                        indicatorColor = SafeRiderCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_ai")
                )

                NavigationBarItem(
                    selected = state.currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.setScreen(AppScreen.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Setelan"
                        )
                    },
                    label = { Text("Setelan", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF090D16),
                        selectedTextColor = SafeRiderCyan,
                        indicatorColor = SafeRiderCyan,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentScreen) {
                AppScreen.ROUTE_PLANNER -> {
                    RoutePlannerScreen(
                        state = state,
                        onSelectRoute = { viewModel.selectRoute(it) },
                        onStartNavigation = { viewModel.startNavigation() },
                        onToggleNight = { viewModel.toggleNightTime() },
                        onSelectVehicle = { viewModel.setVehicleType(it) },
                        onRequestLocationPermission = requestLocationPermission,
                        onDetectGpsLocation = { viewModel.fetchCurrentLocation() },
                        onOpenInGoogleMaps = { viewModel.openInGoogleMapsNavigation(context) },
                        onMapStyleChange = { viewModel.setMapStyleMode(it) },
                        onUpvoteHazard = { viewModel.upvoteHazard(it) }
                    )
                }

                AppScreen.NAVIGATION_HUD -> {
                    NavigationHudScreen(
                        state = state,
                        onStopNavigation = { viewModel.stopNavigation() },
                        onDismissAlert = { viewModel.dismissHazardAlert() },
                        onRepeatVoice = { viewModel.testVoiceAnnouncement() },
                        onToggleMute = { viewModel.toggleVoiceMute() },
                        onTriggerSos = { viewModel.triggerSosManual() },
                        onSimulateAnomalyStop = { viewModel.simulateDarkAreaAnomalyStop() },
                        onTriggerRerouteSim = { viewModel.simulateAdaptiveRerouteTrigger() },
                        onOpenReportDialog = { viewModel.openReportDialog() },
                        onVoiceCommandTap = { isVoiceModalOpen = true },
                        onRequestLocationPermission = requestLocationPermission
                    )
                }

                AppScreen.HAZARDS_MAP -> {
                    HazardsMapScreen(
                        state = state,
                        onFilterChange = { viewModel.filterHazards(it) },
                        onUpvoteHazard = { viewModel.upvoteHazard(it) },
                        onCrossCheckLocation = { viewModel.crossCheckHazardLocation(it) },
                        onOpenReportDialog = { viewModel.openReportDialog() }
                    )
                }

                AppScreen.AI_ASSISTANT -> {
                    AiConsultScreen(
                        state = state,
                        onSendMessage = { viewModel.sendAiQuery(it) }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        state = state,
                        onSelectVehicle = { viewModel.setVehicleType(it) },
                        onTestVoice = { viewModel.testVoiceAnnouncement() },
                        onTriggerSos = { viewModel.triggerSosManual() },
                        onOpenEditContact = { viewModel.openEditContactDialog(it) },
                        onSignInGoogle = {
                            (context as? Activity)?.let { act ->
                                viewModel.signInWithGoogle(act)
                            }
                        },
                        onSignOutGoogle = { viewModel.signOut() }
                    )
                }
            }

            // Global Overlays: Adaptive Mid-Journey Reroute Proposal
            AdaptiveRerouteDialog(
                suggestion = state.activeRerouteSuggestion,
                isSpeaking = state.isSpeaking,
                onAcceptReroute = { viewModel.acceptReroute() },
                onDeclineReroute = { viewModel.declineReroute() }
            )

            // Global Overlays: Emergency SOS Dialog
            EmergencySosDialog(
                isOpen = state.isSosActive,
                countdownSeconds = state.sosCountdown,
                isDispatched = state.isSosDispatched,
                triggerReason = state.sosTriggerReason,
                contacts = state.emergencyContacts,
                isStrobeActive = state.isStrobeActive,
                onCancel = { viewModel.cancelSos() },
                onDispatchNow = { viewModel.dispatchSosImmediately() },
                onToggleStrobe = { viewModel.toggleStrobeBeacon() }
            )

            // Global Overlays: Report Hazard Dialog
            ReportHazardDialog(
                isOpen = state.isReportDialogOpen,
                onDismiss = { viewModel.closeReportDialog() },
                onSubmit = { type, title, location, note ->
                    viewModel.submitHazardReport(type, title, location, note)
                }
            )

            // Global Overlays: Voice Command Modal
            VoiceCommandModal(
                isOpen = isVoiceModalOpen,
                onDismiss = { isVoiceModalOpen = false },
                onExecuteCommand = { cmd ->
                    viewModel.sendAiQuery(cmd)
                }
            )

            // Global Overlays: Location Cross-Check Dialog (Gemini + Google Search)
            LocationCrossCheckDialog(
                hazard = state.selectedCrossCheckHazard,
                result = state.locationCrossCheckResult,
                isLoading = state.isCrossCheckingLocation,
                onDismiss = { viewModel.closeCrossCheckDialog() },
                onUpvote = {
                    state.selectedCrossCheckHazard?.let { h ->
                        viewModel.upvoteHazard(h.id)
                    }
                }
            )

            // Global Overlays: Edit / Add Emergency Contact Dialog
            EditContactDialog(
                contact = state.editingContact,
                isOpen = state.isEditContactDialogOpen,
                onDismiss = { viewModel.closeEditContactDialog() },
                onSaveContact = { contact ->
                    viewModel.saveEmergencyContact(contact)
                },
                onDeleteContact = { contactId ->
                    viewModel.deleteEmergencyContact(contactId)
                }
            )

            // Global Overlays: Location Permission Explanation & Prompt Dialog
            LocationPermissionDialog(
                isOpen = state.showLocationPermissionDialog,
                onGrantPermission = requestLocationPermission,
                onUseSimulation = { viewModel.setSimulationFallback(true) },
                onDismiss = { viewModel.dismissLocationPermissionDialog() }
            )
        }
    }
}

