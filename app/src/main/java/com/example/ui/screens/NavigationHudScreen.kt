package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HazardAlertBanner
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.SafeLightMeter
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderDarkBg
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed
import com.example.viewmodel.SafeRiderUiState

@Composable
fun NavigationHudScreen(
    state: SafeRiderUiState,
    onStopNavigation: () -> Unit,
    onDismissAlert: () -> Unit,
    onRepeatVoice: () -> Unit,
    onToggleMute: () -> Unit,
    onTriggerSos: () -> Unit,
    onSimulateAnomalyStop: () -> Unit,
    onTriggerRerouteSim: () -> Unit,
    onOpenReportDialog: () -> Unit,
    onVoiceCommandTap: () -> Unit,
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val route = state.selectedRoute
    val currentStep = route?.steps?.getOrNull(state.currentStepIndex)
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SafeRiderDarkBg)
            .testTag("navigation_hud_screen")
    ) {
        // 1. Full-screen Real-World Night Map Canvas
        RouteMapCanvas(
            route = route,
            hazards = state.hazards,
            pois = state.realWorldPois,
            mapStyle = state.mapStyleMode,
            progressRatio = state.progressRatio,
            isNavigating = true,
            showInteractiveControls = true,
            onOpenInGoogleMaps = {
                if (route != null) {
                    com.example.data.SafeRiderRepository.openNavigationInGoogleMaps(
                        context = context,
                        destLat = route.destLat,
                        destLng = route.destLng,
                        originLat = state.userLatitude ?: route.startLat,
                        originLng = state.userLongitude ?: route.startLng
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Maneuver & Navigation Guidance Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(14.dp)
        ) {
            // Adaptive Live Monitoring Status Pill & Real-time GPS Tracker indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(
                        1.dp,
                        if (state.isGpsTrackingActive) SafeRiderCyan.copy(alpha = 0.6f) else SafeRiderAmber.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        if (!state.isGpsTrackingActive) {
                            onRequestLocationPermission()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("gps_tracking_status_pill"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isGpsTrackingActive) SafeRiderGreen else SafeRiderAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (state.isGpsTrackingActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                        contentDescription = null,
                        tint = if (state.isGpsTrackingActive) SafeRiderCyan else SafeRiderAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.isGpsTrackingActive) {
                            "GPS LIVE (${if (state.isFineLocation) "Presisi Tinggi" else "Perkiraan"}) • ±${state.userAccuracyMeters?.toInt() ?: 4}m"
                        } else {
                            "MODE SIMULASI • KETUK AKTIFKAN GPS ASLI"
                        },
                        color = if (state.isGpsTrackingActive) SafeRiderCyan else SafeRiderAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = "${state.hazards.size} Hazard Rute",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1829).copy(alpha = 0.95f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, SafeRiderCyan.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .testTag("turn_guidance_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SafeRiderCyan.copy(alpha = 0.2f))
                            .border(1.dp, SafeRiderCyan, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentStep?.maneuverIcon == "turn_right") Icons.Default.TurnRight else Icons.Default.Navigation,
                            contentDescription = null,
                            tint = SafeRiderCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentStep?.instruction ?: "Tetap di jalur SafeLight",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentStep?.distanceMeters ?: 400}m lagi",
                                color = SafeRiderCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${currentStep?.streetName ?: ""}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Sound speaking wave indicator
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("hud_mute_button")
                    ) {
                        Icon(
                            imageVector = if (state.isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute Suara",
                            tint = if (state.isSpeaking) SafeRiderCyan else if (state.isVoiceMuted) Color.Gray else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hazard Alert Banner right below top maneuver card
            HazardAlertBanner(
                alert = state.activeHazardAlert,
                isSpeaking = state.isSpeaking,
                onDismiss = onDismissAlert,
                onRepeatVoice = onRepeatVoice
            )
        }

        // 3. Floating Quick Safety Actions (Right Edge)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Adaptive reroute simulation button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F2438).copy(alpha = 0.95f))
                    .border(1.dp, SafeRiderCyan, RoundedCornerShape(12.dp))
                    .clickable { onTriggerRerouteSim() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("simulate_adaptive_reroute_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = SafeRiderCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tes Reroute Cerdas",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Anomaly stop test button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E283D).copy(alpha = 0.9f))
                    .border(1.dp, SafeRiderOrange.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { onSimulateAnomalyStop() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("simulate_dark_stop_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = SafeRiderOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tes Berhenti Gelap", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Report Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                    .border(1.5.dp, SafeRiderAmber, CircleShape)
                    .clickable { onOpenReportDialog() }
                    .testTag("hud_quick_report_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = "Lapor Bahaya",
                    tint = SafeRiderAmber,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Mic Voice Command Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SafeRiderCyan.copy(alpha = 0.2f))
                    .border(1.5.dp, SafeRiderCyan, CircleShape)
                    .clickable { onVoiceCommandTap() }
                    .testTag("hud_voice_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Perintah Suara",
                    tint = SafeRiderCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 4. Bottom Rider Dashboard Panel (Speedometer + Illumination Meter + SOS)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF090D16).copy(alpha = 0.92f),
                            Color(0xFF090D16)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Speedometer & Illumination Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111A2B).copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speedometer
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.currentSpeedKmH}",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = "km/jam",
                            color = SafeRiderCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Batas 60",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }
                }

                // Compact Illumination Meter
                SafeLightMeter(
                    score = state.currentIlluminationScore,
                    compact = true
                )

                // ETA & Remaining Distance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${state.etaMinutesRemaining} Menit",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${String.format("%.1f", state.distanceRemainingMeters / 1000f)} km lagi",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: SOS & End Ride
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Emergency SOS Big Red Button
                Button(
                    onClick = onTriggerSos,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRiderRed),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("hud_sos_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DARURAT SOS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Stop Navigation Button
                Button(
                    onClick = onStopNavigation,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("hud_stop_nav_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Akhiri",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Akhiri",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
