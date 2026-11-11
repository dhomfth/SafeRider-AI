package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RouteOption
import com.example.ui.components.RouteMapCanvas
import com.example.ui.components.SafeLightMeter
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderDarkBg
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed
import com.example.model.MapStyleMode
import com.example.viewmodel.SafeRiderUiState

@Composable
fun RoutePlannerScreen(
    state: SafeRiderUiState,
    onSelectRoute: (RouteOption) -> Unit,
    onStartNavigation: () -> Unit,
    onToggleNight: () -> Unit,
    onSelectVehicle: (String) -> Unit,
    onRequestLocationPermission: () -> Unit = {},
    onDetectGpsLocation: () -> Unit = {},
    onOpenInGoogleMaps: (() -> Unit)? = null,
    onMapStyleChange: ((MapStyleMode) -> Unit)? = null,
    onUpvoteHazard: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isMapExpanded by remember { mutableStateOf(false) }
    val mapHeight by animateDpAsState(
        targetValue = if (isMapExpanded) 340.dp else 210.dp,
        label = "minimapHeight"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SafeRiderDarkBg)
            .testTag("route_planner_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with App Title & Night Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SafeRiderCyan.copy(alpha = 0.2f))
                                .border(1.dp, SafeRiderCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = SafeRiderCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SafeRider AI",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Navigasi Cerdas & Keamanan Malam Hari",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                // Night/Day and Vehicle Mode Chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (state.isNightTime) Color(0xFF1E293B) else Color(0xFF0F766E))
                            .clickable { onToggleNight() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("night_mode_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isNightTime) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (state.isNightTime) SafeRiderAmber else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isNightTime) "Malam" else "Siang",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                onSelectVehicle(if (state.vehicleType == "Sepeda Motor") "Mobil" else "Sepeda Motor")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("vehicle_type_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.vehicleType == "Sepeda Motor") Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = SafeRiderCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.vehicleType == "Sepeda Motor") "Motor" else "Mobil",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Origin & Destination Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF24364F), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(SafeRiderGreen)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Dari (Lokasi Anda)", color = Color(0xFF64748B), fontSize = 10.sp)
                                Text(
                                    text = if (state.hasLocationPermission && state.userLatitude != null) {
                                        "Posisi GPS (${"%.4f".format(state.userLatitude)}, ${"%.4f".format(state.userLongitude ?: 0.0)})"
                                    } else {
                                        state.origin
                                    },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // GPS Status badge button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (state.hasLocationPermission) SafeRiderGreen.copy(alpha = 0.15f) else SafeRiderCyan.copy(alpha = 0.15f))
                                .border(1.dp, if (state.hasLocationPermission) SafeRiderGreen else SafeRiderCyan, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (state.hasLocationPermission) {
                                        onDetectGpsLocation()
                                    } else {
                                        onRequestLocationPermission()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("gps_badge_route_planner")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (state.hasLocationPermission) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                    contentDescription = null,
                                    tint = if (state.hasLocationPermission) SafeRiderGreen else SafeRiderCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.hasLocationPermission) "GPS Aktif" else "Deteksi GPS",
                                    color = if (state.hasLocationPermission) SafeRiderGreen else SafeRiderCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp)
                            .width(2.dp)
                            .height(18.dp)
                            .background(Color(0xFF334155))
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(SafeRiderCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Tujuan", color = Color(0xFF64748B), fontSize = 10.sp)
                            Text(state.destination, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3. Interactive Mini Map Preview (Expandable with Zoom In/Out & Pan)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mapHeight)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF26354D), RoundedCornerShape(18.dp))
            ) {
                RouteMapCanvas(
                    route = state.selectedRoute,
                    hazards = state.hazards,
                    pois = state.realWorldPois,
                    mapStyle = state.mapStyleMode,
                    onMapStyleChange = onMapStyleChange,
                    progressRatio = 0.05f,
                    isNavigating = false,
                    showInteractiveControls = true,
                    onOpenInGoogleMaps = onOpenInGoogleMaps,
                    onUpvoteHazard = onUpvoteHazard
                )

                // Top banner with instructions and full-screen / expand toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                            .border(1.dp, SafeRiderCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔍 Zoom In/Out • Geser • Ketuk Bahaya",
                            color = SafeRiderCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .clickable { isMapExpanded = !isMapExpanded }
                            .testTag("toggle_expand_minimap_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMapExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Perbesar/Kecilkan Minimap",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. SafeLight Illumination Breakdown for Selected Route
        item {
            state.selectedRoute?.let { route ->
                SafeLightMeter(score = route.illuminationScore)
            }
        }

        // 5. Route Options Selection (Fastest vs SafeLight)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PILIHAN RUTE PERJALANAN",
                    color = SafeRiderCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${state.availableRoutes.size} Jalur Dianalisis",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        items(state.availableRoutes) { route ->
            val isSelected = state.selectedRoute?.id == route.id
            RouteComparisonCard(
                route = route,
                isSelected = isSelected,
                onClick = { onSelectRoute(route) }
            )
        }

        // 6. Big Start Navigation Action & Google Maps Integration
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartNavigation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.selectedRoute?.isRecommended == true) SafeRiderCyan else SafeRiderGreen
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .testTag("start_navigation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color(0xFF090D16),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "MULAI NAVIGASI",
                            color = Color(0xFF090D16),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${state.selectedRoute?.durationMinutes ?: 18} Mnt • SafeLight",
                            color = Color(0xFF00363D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Direct Google Maps Action
                Button(
                    onClick = { onOpenInGoogleMaps?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(60.dp)
                        .border(1.dp, Color(0xFF1A73E8), RoundedCornerShape(18.dp))
                        .testTag("planner_open_google_maps_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFF1A73E8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Peta Nyata",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RouteComparisonCard(
    route: RouteOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected && route.isRecommended -> SafeRiderCyan
            isSelected -> SafeRiderGreen
            route.isRecommended -> Color(0xFF00E5FF).copy(alpha = 0.4f)
            else -> Color(0xFF1E293B)
        },
        label = "routeBorder"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF141F33) else Color(0xFF0F1726)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("route_card_${route.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (route.isRecommended) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeRiderCyan)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "★ REKOMENDASI MALAM",
                                color = Color(0xFF090D16),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (route.routeType == "FASTEST") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SafeRiderOrange)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TERCEPAT • MINIM LAMPU",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Illumination Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (route.illuminationScore >= 7f) SafeRiderCyan else SafeRiderOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Skor ${route.illuminationScore}/10",
                        color = if (route.illuminationScore >= 7f) SafeRiderCyan else SafeRiderOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = route.subtitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${route.durationMinutes} Menit",
                        color = if (isSelected) SafeRiderCyan else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${route.distanceKm} km",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Reasoning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (route.isRecommended) Color(0xFF00363D).copy(alpha = 0.35f) else Color(0xFF261214).copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        if (route.isRecommended) SafeRiderCyan.copy(alpha = 0.3f) else SafeRiderOrange.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = if (route.isRecommended) Icons.Default.Shield else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (route.isRecommended) SafeRiderCyan else SafeRiderOrange,
                        modifier = Modifier.size(16.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = route.aiReasoning,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
