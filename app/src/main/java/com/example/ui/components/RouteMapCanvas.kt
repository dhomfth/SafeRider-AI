package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SafeRiderRepository
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.example.model.MapStyleMode
import com.example.model.RealWorldPoi
import com.example.model.RouteOption
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed
import kotlin.math.roundToInt

@Composable
fun RouteMapCanvas(
    route: RouteOption?,
    hazards: List<HazardMarker>,
    pois: List<RealWorldPoi> = SafeRiderRepository.sampleRealWorldPois,
    mapStyle: MapStyleMode = MapStyleMode.REAL_WORLD,
    onMapStyleChange: ((MapStyleMode) -> Unit)? = null,
    progressRatio: Float = 0f,
    isNavigating: Boolean = false,
    showInteractiveControls: Boolean = true,
    userGpsCoordinates: String? = null,
    onHazardSelected: ((HazardMarker) -> Unit)? = null,
    onPoiSelected: ((RealWorldPoi) -> Unit)? = null,
    onOpenInGoogleMaps: (() -> Unit)? = null,
    onUpvoteHazard: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMapStyle by remember { mutableStateOf(mapStyle) }

    // Zoom & Pan interactive states
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedHazard by remember { mutableStateOf<HazardMarker?>(null) }
    var selectedPoi by remember { mutableStateOf<RealWorldPoi?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (currentMapStyle) {
                    MapStyleMode.REAL_WORLD -> Color(0xFF0C1322)
                    MapStyleMode.TACTICAL -> Color(0xFF070B12)
                    MapStyleMode.SATELLITE -> Color(0xFF080F17)
                }
            )
            .testTag("route_map_canvas")
            // Multi-touch gestures: Pinch to zoom & two-finger pan
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 4.5f)
                    panOffset += pan
                }
            }
            // Tap gesture to select hazards or POIs
            .pointerInput(hazards, pois, scale, panOffset) {
                detectTapGestures { tapOffset ->
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)

                    // Invert canvas transform to hit-test items in unscaled canvas coordinates
                    val unscaledX = (tapOffset.x - center.x - panOffset.x) / scale + center.x
                    val unscaledY = (tapOffset.y - center.y - panOffset.y) / scale + center.y

                    val hitRadius = 38f

                    // 1. Check Hazards hit test
                    val tappedHazard = hazards.firstOrNull { hazard ->
                        val hx = hazard.x * w
                        val hy = hazard.y * h
                        val distSq = (unscaledX - hx) * (unscaledX - hx) + (unscaledY - hy) * (unscaledY - hy)
                        distSq <= hitRadius * hitRadius
                    }

                    // 2. Check POIs hit test
                    val tappedPoi = if (tappedHazard == null) {
                        pois.firstOrNull { poi ->
                            val px = poi.x * w
                            val py = poi.y * h
                            val distSq = (unscaledX - px) * (unscaledX - px) + (unscaledY - py) * (unscaledY - py)
                            distSq <= hitRadius * hitRadius
                        }
                    } else null

                    if (tappedHazard != null) {
                        selectedHazard = tappedHazard
                        selectedPoi = null
                        onHazardSelected?.invoke(tappedHazard)
                    } else if (tappedPoi != null) {
                        selectedPoi = tappedPoi
                        selectedHazard = null
                        onPoiSelected?.invoke(tappedPoi)
                    } else {
                        // Tapping background dismisses cards
                        selectedHazard = null
                        selectedPoi = null
                    }
                }
            }
    ) {
        // 1. Map Canvas with realistic real-world street network and layers
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            withTransform({
                translate(panOffset.x, panOffset.y)
                scale(scale, scale, center)
            }) {
                when (currentMapStyle) {
                    MapStyleMode.REAL_WORLD -> {
                        drawRealWorldCityBase(w, h)
                        drawWaterBodies(w, h)
                        drawCityParks(w, h)
                        drawRealWorldStreets(w, h)
                        drawTransitLines(w, h)
                        drawStreetNamesLabels(w, h)
                    }
                    MapStyleMode.TACTICAL -> {
                        drawNightCityGrid(w, h)
                        drawTacticalRoadNetwork(w, h)
                    }
                    MapStyleMode.SATELLITE -> {
                        drawSatelliteBase(w, h)
                        drawRealWorldStreets(w, h, isSatellite = true)
                    }
                }

                // Route Polyline with SafeLight illumination glow
                if (route != null && route.pathPoints.isNotEmpty()) {
                    drawRoutePolyline(route, w, h, isNavigating, progressRatio)
                }

                // Real-World POIs (SPBU 24h, Pos Polisi, RS)
                if (currentMapStyle == MapStyleMode.REAL_WORLD) {
                    drawRealWorldPoiPins(pois, w, h, selectedPoi)
                }

                // Hazard Markers
                drawHazardPins(hazards, w, h, selectedHazard)

                // Start & Destination pins
                if (route != null && route.pathPoints.isNotEmpty()) {
                    val start = route.pathPoints.first()
                    val end = route.pathPoints.last()
                    drawLocationPin(start.x * w, start.y * h, "A", SafeRiderGreen, "Mulai (Medan Merdeka)")
                    drawLocationPin(end.x * w, end.y * h, "B", SafeRiderCyan, "Tujuan (Gatot Subroto)")
                }

                // Rider Vehicle Marker with headlight cone
                if (route != null && route.pathPoints.isNotEmpty()) {
                    val riderPos = calculateRiderPosition(route, progressRatio, w, h)
                    drawRiderMarker(
                        x = riderPos.x,
                        y = riderPos.y,
                        heading = riderPos.heading,
                        pulseRadius = pulseRadius,
                        pulseAlpha = pulseAlpha,
                        isNavigating = isNavigating
                    )
                }
            }
        }

        // 2. Top-Left: Map Layer Style Selector (Jalan Nyata / Taktis PJU / Satelit)
        if (showInteractiveControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.94f))
                    .border(1.dp, SafeRiderCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MapStyleMode.entries.forEach { mode ->
                    val isSelected = currentMapStyle == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SafeRiderCyan else Color.Transparent)
                            .clickable {
                                currentMapStyle = mode
                                onMapStyleChange?.invoke(mode)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("map_style_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.shortName,
                            color = if (isSelected) Color(0xFF090E17) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Top-Right: Open in Google Maps Navigation Button (Buka di Google Maps Nyata)
        if (showInteractiveControls) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp, top = 12.dp)
            ) {
                Button(
                    onClick = {
                        if (onOpenInGoogleMaps != null) {
                            onOpenInGoogleMaps()
                        } else if (route != null) {
                            SafeRiderRepository.openNavigationInGoogleMaps(
                                context = context,
                                destLat = route.destLat,
                                destLng = route.destLng,
                                originLat = route.startLat,
                                originLng = route.startLng
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_in_google_maps_fab")
                ) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = "Buka di Google Maps",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Buka di Google Maps",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Interactive Zoom In / Zoom Out / Recenter Controls
        if (showInteractiveControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom In Button (+)
                InteractiveMapButton(
                    onClick = {
                        scale = (scale * 1.25f).coerceAtMost(4.5f)
                    },
                    testTag = "map_zoom_in_button"
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = SafeRiderCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Level Percentage Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(scale * 100).roundToInt()}%",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Zoom Out Button (-)
                InteractiveMapButton(
                    onClick = {
                        scale = (scale / 1.25f).coerceAtLeast(0.5f)
                    },
                    testTag = "map_zoom_out_button"
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = SafeRiderCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Recenter / Reset Button (🎯)
                InteractiveMapButton(
                    onClick = {
                        scale = 1.0f
                        panOffset = Offset.Zero
                    },
                    testTag = "map_recenter_button"
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "Pusatkan Peta",
                        tint = SafeRiderGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 5. Floating Selected Hazard Tooltip / Card
        AnimatedVisibility(
            visible = selectedHazard != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            selectedHazard?.let { hazard ->
                HazardInspectionCard(
                    hazard = hazard,
                    onDismiss = { selectedHazard = null },
                    onUpvote = {
                        onUpvoteHazard?.invoke(hazard.id)
                    },
                    onOpenInGoogleMaps = {
                        val lat = hazard.latitude ?: (-6.1754 + (hazard.y - 0.5) * 0.04)
                        val lng = hazard.longitude ?: (106.8272 + (hazard.x - 0.5) * 0.04)
                        SafeRiderRepository.openPlaceInGoogleMaps(
                            context = context,
                            latitude = lat,
                            longitude = lng,
                            title = hazard.title
                        )
                    }
                )
            }
        }

        // 6. Floating Selected POI Tooltip / Card
        AnimatedVisibility(
            visible = selectedPoi != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            selectedPoi?.let { poi ->
                PoiInspectionCard(
                    poi = poi,
                    onDismiss = { selectedPoi = null },
                    onOpenInGoogleMaps = {
                        SafeRiderRepository.openPlaceInGoogleMaps(
                            context = context,
                            latitude = poi.latitude,
                            longitude = poi.longitude,
                            title = poi.name
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun InteractiveMapButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A).copy(alpha = 0.92f))
            .border(1.dp, SafeRiderCyan.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun HazardInspectionCard(
    hazard: HazardMarker,
    onDismiss: () -> Unit,
    onUpvote: () -> Unit,
    onOpenInGoogleMaps: () -> Unit
) {
    val hazardColor = when (hazard.type) {
        HazardType.PJU_MATI -> SafeRiderAmber
        HazardType.JALAN_BERLUBANG -> SafeRiderOrange
        HazardType.RAWAN_BEGAL -> SafeRiderRed
        HazardType.BLIND_SPOT -> Color(0xFFE040FB)
        HazardType.GENANGAN_AIR -> Color(0xFF00B0FF)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, hazardColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .testTag("hazard_inspection_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(hazardColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (hazard.type) {
                            HazardType.PJU_MATI -> Icons.Default.FlashlightOff
                            HazardType.JALAN_BERLUBANG -> Icons.Default.Warning
                            HazardType.RAWAN_BEGAL -> Icons.Default.Shield
                            HazardType.BLIND_SPOT -> Icons.Default.VisibilityOff
                            HazardType.GENANGAN_AIR -> Icons.Default.WaterDrop
                        }
                        Icon(icon, contentDescription = null, tint = hazardColor, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = hazard.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = hazard.locationName,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action recommendation & Illumination score pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💡 Skor Terang: ${hazard.illuminationScore}/10",
                    color = hazardColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${hazard.upvotes} Laporan Pengendara",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "🛡️ ${hazard.recommendedAction}",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button Buka di Google Maps
                Button(
                    onClick = onOpenInGoogleMaps,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("hazard_open_google_maps_btn")
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = SafeRiderCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Peta Google", color = SafeRiderCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Upvote confirmation button
                Button(
                    onClick = onUpvote,
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRiderCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("hazard_upvote_button")
                ) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Konfirmasi (+1)", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PoiInspectionCard(
    poi: RealWorldPoi,
    onDismiss: () -> Unit,
    onOpenInGoogleMaps: () -> Unit
) {
    val poiColor = when (poi.category) {
        "SPBU_24H" -> SafeRiderAmber
        "POS_POLISI" -> SafeRiderCyan
        "RS_IGD" -> SafeRiderRed
        else -> SafeRiderGreen
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.96f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, poiColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .testTag("poi_inspection_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(poiColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (poi.category) {
                            "SPBU_24H" -> Icons.Default.LocalGasStation
                            "POS_POLISI" -> Icons.Default.LocalPolice
                            "RS_IGD" -> Icons.Default.LocalHospital
                            else -> Icons.Default.Shield
                        }
                        Icon(icon, contentDescription = null, tint = poiColor, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = poi.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (poi.isOpen24Hours) "Buka 24 Jam • Area Aman Terang" else "Titik Aman Rute",
                            color = SafeRiderGreen,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = poi.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onOpenInGoogleMaps,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("poi_open_google_maps_btn")
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buka di Google Maps Nyata", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- DrawScope Drawing Helpers for Realistic Map ---

private fun DrawScope.drawRealWorldCityBase(w: Float, h: Float) {
    // City block grid with subtle building outlines
    val blockColor = Color(0xFF101927)
    val strokeColor = Color(0xFF162338)

    val blockSize = 70f
    var bx = -w
    while (bx < w * 2) {
        var by = -h
        while (by < h * 2) {
            drawRect(
                color = blockColor,
                topLeft = Offset(bx + 4f, by + 4f),
                size = Size(blockSize - 8f, blockSize - 8f)
            )
            drawRect(
                color = strokeColor,
                topLeft = Offset(bx + 4f, by + 4f),
                size = Size(blockSize - 8f, blockSize - 8f),
                style = Stroke(width = 1f)
            )
            by += blockSize
        }
        bx += blockSize
    }
}

private fun DrawScope.drawWaterBodies(w: Float, h: Float) {
    // Sungai Ciliwung & Banjir Kanal Barat Realistic Flow
    val waterPath = Path().apply {
        moveTo(w * 0.75f, -h)
        cubicTo(w * 0.70f, h * 0.25f, w * 0.65f, h * 0.50f, w * 0.72f, h * 0.75f)
        cubicTo(w * 0.78f, h * 1.0f, w * 0.70f, h * 1.5f, w * 0.65f, h * 2.0f)
    }

    // Outer water glow
    drawPath(
        path = waterPath,
        color = Color(0x330288D1),
        style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner river line
    drawPath(
        path = waterPath,
        color = Color(0xFF0F3150),
        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawCityParks(w: Float, h: Float) {
    // Medan Merdeka / Monas Park (green area at top-left)
    val parkColor = Color(0xFF0B251A)
    val parkBorder = Color(0xFF123D2B)
    drawRoundRect(
        color = parkColor,
        topLeft = Offset(w * 0.08f, h * 0.80f),
        size = Size(w * 0.16f, h * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = parkBorder,
        topLeft = Offset(w * 0.08f, h * 0.80f),
        size = Size(w * 0.16f, h * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1.5f)
    )

    // Taman Suropati / Menteng Park
    drawRoundRect(
        color = parkColor,
        topLeft = Offset(w * 0.35f, h * 0.62f),
        size = Size(w * 0.12f, h * 0.08f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
}

private fun DrawScope.drawRealWorldStreets(w: Float, h: Float, isSatellite: Boolean = false) {
    val majorRoadBase = if (isSatellite) Color(0xFF263238) else Color(0xFF1E2C42)
    val majorRoadCenter = if (isSatellite) Color(0xFF37474F) else Color(0xFF2B3D5B)
    val arterialRoad = Color(0xFF384E73)
    val centerlineYellow = Color(0xFFFFD54F).copy(alpha = 0.6f)

    // 1. Primary Corridor: Jl. Thamrin - Sudirman (Diagonal Main Axis)
    val pStart = Offset(w * 0.15f, h * 0.85f)
    val pBundaranHi = Offset(w * 0.40f, h * 0.55f)
    val pSudirman = Offset(w * 0.58f, h * 0.40f)
    val pSemanggi = Offset(w * 0.72f, h * 0.28f)
    val pGatsu = Offset(w * 0.85f, h * 0.15f)

    // Base wide road bed
    val corridorPath = Path().apply {
        moveTo(pStart.x, pStart.y)
        lineTo(pBundaranHi.x, pBundaranHi.y)
        lineTo(pSudirman.x, pSudirman.y)
        lineTo(pSemanggi.x, pSemanggi.y)
        lineTo(pGatsu.x, pGatsu.y)
    }

    // Outer road width
    drawPath(corridorPath, majorRoadBase, style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(corridorPath, arterialRoad, style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(corridorPath, majorRoadCenter, style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    // Center divider dash
    drawPath(
        corridorPath,
        centerlineYellow,
        style = Stroke(
            width = 1.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
    )

    // 2. Bundaran HI Roundabout (Circle)
    drawCircle(color = majorRoadBase, radius = 24f, center = pBundaranHi)
    drawCircle(color = arterialRoad, radius = 18f, center = pBundaranHi, style = Stroke(width = 12f))
    drawCircle(color = Color(0xFF0F3150), radius = 10f, center = pBundaranHi) // Water fountain

    // 3. Simpang Susun Semanggi (Cloverleaf Loops)
    drawCircle(color = majorRoadBase, radius = 28f, center = pSemanggi, style = Stroke(width = 8f))
    drawCircle(color = arterialRoad, radius = 20f, center = pSemanggi, style = Stroke(width = 6f))

    // 4. Cross Arterials (Gatot Subroto, Rasuna Said, Kebon Sirih)
    // Jl. Gatot Subroto (horizontal arterial)
    drawLine(majorRoadBase, Offset(-w, pGatsu.y), Offset(w * 2, pGatsu.y), strokeWidth = 20f)
    drawLine(arterialRoad, Offset(-w, pGatsu.y), Offset(w * 2, pGatsu.y), strokeWidth = 14f)

    // Jl. HR Rasuna Said / Kuningan (vertical arterial)
    drawLine(majorRoadBase, Offset(w * 0.65f, -h), Offset(w * 0.65f, h * 2), strokeWidth = 18f)
    drawLine(arterialRoad, Offset(w * 0.65f, -h), Offset(w * 0.65f, h * 2), strokeWidth = 12f)

    // Jl. Casablanca / Prof. Dr. Satrio (flyover horizontal)
    drawLine(majorRoadBase, Offset(-w, h * 0.38f), Offset(w * 2, h * 0.38f), strokeWidth = 16f)
    drawLine(arterialRoad, Offset(-w, h * 0.38f), Offset(w * 2, h * 0.38f), strokeWidth = 10f)

    // Jl. Kebon Sirih
    drawLine(majorRoadBase, Offset(-w, h * 0.75f), Offset(w * 2, h * 0.75f), strokeWidth = 14f)
    drawLine(arterialRoad, Offset(-w, h * 0.75f), Offset(w * 2, h * 0.75f), strokeWidth = 8f)
}

private fun DrawScope.drawTransitLines(w: Float, h: Float) {
    // MRT Jakarta Line (Cyan dashed transit overlay)
    val mrtPath = Path().apply {
        moveTo(w * 0.15f, h * 0.85f)
        lineTo(w * 0.40f, h * 0.55f)
        lineTo(w * 0.58f, h * 0.40f)
        lineTo(w * 0.72f, h * 0.28f)
    }
    drawPath(
        path = mrtPath,
        color = SafeRiderCyan.copy(alpha = 0.4f),
        style = Stroke(
            width = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    )
}

private fun DrawScope.drawStreetNamesLabels(w: Float, h: Float) {
    val paint = Paint().apply {
        color = android.graphics.Color.argb(190, 203, 213, 225)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val native = drawContext.canvas.nativeCanvas
    native.drawText("Jl. MH Thamrin", w * 0.26f, h * 0.68f, paint)
    native.drawText("Bundaran HI", w * 0.42f, h * 0.53f, paint)
    native.drawText("Jl. Jend. Sudirman", w * 0.59f, h * 0.42f, paint)
    native.drawText("Semanggi Flyover", w * 0.73f, h * 0.30f, paint)
    native.drawText("Jl. Gatot Subroto", w * 0.40f, h * 0.14f, paint)
    native.drawText("Rasuna Said Kuningan", w * 0.66f, h * 0.60f, paint)
}

private fun DrawScope.drawNightCityGrid(w: Float, h: Float) {
    val gridColor = Color(0xFF131D2E)
    val step = 60f
    var x = -w
    while (x < w * 2) {
        drawLine(
            color = gridColor,
            start = Offset(x, -h),
            end = Offset(x, h * 2),
            strokeWidth = 1f
        )
        x += step
    }
    var y = -h
    while (y < h * 2) {
        drawLine(
            color = gridColor,
            start = Offset(-w, y),
            end = Offset(w * 2, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawTacticalRoadNetwork(w: Float, h: Float) {
    val roadColor = Color(0xFF1B283D)
    val arterialRoadColor = Color(0xFF243652)

    drawLine(roadColor, Offset(-w, h * 0.35f), Offset(w * 2, h * 0.35f), strokeWidth = 8f)
    drawLine(roadColor, Offset(-w, h * 0.70f), Offset(w * 2, h * 0.70f), strokeWidth = 8f)
    drawLine(roadColor, Offset(w * 0.30f, -h), Offset(w * 0.30f, h * 2), strokeWidth = 8f)
    drawLine(roadColor, Offset(w * 0.65f, -h), Offset(w * 0.65f, h * 2), strokeWidth = 8f)

    drawLine(arterialRoadColor, Offset(w * 0.10f, h * 0.90f), Offset(w * 0.90f, h * 0.10f), strokeWidth = 14f)
    drawLine(Color(0xFF0F172A), Offset(w * 0.10f, h * 0.90f), Offset(w * 0.90f, h * 0.10f), strokeWidth = 10f)
}

private fun DrawScope.drawSatelliteBase(w: Float, h: Float) {
    // Topographic night topography with dark vegetation and illuminated arterial corridors
    drawRect(Color(0xFF091118), Offset(-w, -h), Size(w * 3, h * 3))
    // Urban night clusters
    drawCircle(Color(0xFF13222F), radius = 180f, center = Offset(w * 0.40f, h * 0.55f))
    drawCircle(Color(0xFF101E2B), radius = 140f, center = Offset(w * 0.72f, h * 0.28f))
}

private fun DrawScope.drawRoutePolyline(
    route: RouteOption,
    w: Float,
    h: Float,
    isNavigating: Boolean,
    progress: Float
) {
    val points = route.pathPoints
    if (points.size < 2) return

    val path = Path()
    path.moveTo(points[0].x * w, points[0].y * h)
    for (i in 1 until points.size) {
        path.lineTo(points[i].x * w, points[i].y * h)
    }

    val isBright = route.illuminationScore >= 7.0f
    val baseColor = if (isBright) SafeRiderCyan else SafeRiderOrange
    val glowColor = if (isBright) Color(0x3300E5FF) else Color(0x33FF7A00)

    // Outer glow
    drawPath(
        path = path,
        color = glowColor,
        style = Stroke(
            width = 18f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Main line (dashed if dark/dangerous)
    val strokeStyle = if (!isBright) {
        Stroke(
            width = 8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
        )
    } else {
        Stroke(
            width = 8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    }

    drawPath(
        path = path,
        color = baseColor,
        style = strokeStyle
    )
}

private fun DrawScope.drawHazardPins(
    hazards: List<HazardMarker>,
    w: Float,
    h: Float,
    selectedHazard: HazardMarker?
) {
    hazards.forEach { hazard ->
        val x = hazard.x * w
        val y = hazard.y * h
        val isSelected = hazard.id == selectedHazard?.id

        val pinColor = when (hazard.type) {
            HazardType.PJU_MATI -> SafeRiderAmber
            HazardType.JALAN_BERLUBANG -> SafeRiderOrange
            HazardType.RAWAN_BEGAL -> SafeRiderRed
            HazardType.BLIND_SPOT -> Color(0xFFE040FB)
            HazardType.GENANGAN_AIR -> Color(0xFF00B0FF)
        }

        // Pulse warning or selection ring
        drawCircle(
            color = pinColor.copy(alpha = if (isSelected) 0.5f else 0.25f),
            radius = if (isSelected) 24f else 18f,
            center = Offset(x, y)
        )

        drawCircle(
            color = pinColor,
            radius = if (isSelected) 12f else 9f,
            center = Offset(x, y)
        )

        drawCircle(
            color = Color.White,
            radius = if (isSelected) 5f else 4f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawRealWorldPoiPins(
    pois: List<RealWorldPoi>,
    w: Float,
    h: Float,
    selectedPoi: RealWorldPoi?
) {
    pois.forEach { poi ->
        val x = poi.x * w
        val y = poi.y * h
        val isSelected = poi.id == selectedPoi?.id

        val poiColor = when (poi.category) {
            "SPBU_24H" -> SafeRiderAmber
            "POS_POLISI" -> SafeRiderCyan
            "RS_IGD" -> SafeRiderRed
            else -> SafeRiderGreen
        }

        // Halo ring
        drawCircle(
            color = poiColor.copy(alpha = if (isSelected) 0.5f else 0.20f),
            radius = if (isSelected) 22f else 16f,
            center = Offset(x, y)
        )

        drawCircle(
            color = Color(0xFF0F172A),
            radius = 11f,
            center = Offset(x, y)
        )

        drawCircle(
            color = poiColor,
            radius = 8f,
            center = Offset(x, y)
        )

        drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawLocationPin(
    x: Float,
    y: Float,
    label: String,
    pinColor: Color,
    streetLabel: String = ""
) {
    drawCircle(
        color = pinColor.copy(alpha = 0.3f),
        radius = 20f,
        center = Offset(x, y)
    )
    drawCircle(
        color = pinColor,
        radius = 12f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color(0xFF070B12),
        radius = 6f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawRiderMarker(
    x: Float,
    y: Float,
    heading: Float,
    pulseRadius: Float,
    pulseAlpha: Float,
    isNavigating: Boolean
) {
    // Pulse animation aura
    drawCircle(
        color = SafeRiderCyan.copy(alpha = pulseAlpha * 0.6f),
        radius = pulseRadius,
        center = Offset(x, y)
    )

    // Outer ring
    drawCircle(
        color = SafeRiderCyan,
        radius = 14f,
        center = Offset(x, y),
        style = Stroke(width = 3f)
    )

    // Vehicle Core
    drawCircle(
        color = Color(0xFF0A0F1D),
        radius = 11f,
        center = Offset(x, y)
    )

    drawCircle(
        color = SafeRiderGreen,
        radius = 6f,
        center = Offset(x, y)
    )

    // Headlight Beam Cone
    if (isNavigating) {
        val beamLength = 70f
        val spread = 0.5f // radians
        val radHeading = Math.toRadians(heading.toDouble() - 90.0)

        val leftAngle = radHeading - spread
        val rightAngle = radHeading + spread

        val p1 = Offset(x, y)
        val p2 = Offset(
            x + (beamLength * Math.cos(leftAngle)).toFloat(),
            y + (beamLength * Math.sin(leftAngle)).toFloat()
        )
        val p3 = Offset(
            x + (beamLength * Math.cos(rightAngle)).toFloat(),
            y + (beamLength * Math.sin(rightAngle)).toFloat()
        )

        val beamPath = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        }

        drawPath(
            path = beamPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x99FFF9C4), // Bright light cone
                    Color(0x22FFF59D),
                    Color(0x00FFF59D)
                ),
                center = Offset(x, y),
                radius = beamLength
            )
        )
    }
}

private data class RiderPosition(val x: Float, val y: Float, val heading: Float)

private fun calculateRiderPosition(
    route: RouteOption,
    progress: Float,
    w: Float,
    h: Float
): RiderPosition {
    val points = route.pathPoints
    if (points.isEmpty()) return RiderPosition(0f, 0f, 0f)
    if (points.size == 1) return RiderPosition(points[0].x * w, points[0].y * h, 0f)

    val totalSegments = points.size - 1
    val scaledProgress = (progress * totalSegments).coerceIn(0f, totalSegments.toFloat())
    val currentSegment = scaledProgress.toInt().coerceAtMost(totalSegments - 1)
    val segmentProgress = scaledProgress - currentSegment

    val pA = points[currentSegment]
    val pB = points[currentSegment + 1]

    val curX = (pA.x + (pB.x - pA.x) * segmentProgress) * w
    val curY = (pA.y + (pB.y - pA.y) * segmentProgress) * h

    val dx = (pB.x - pA.x) * w
    val dy = (pB.y - pA.y) * h
    val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()

    return RiderPosition(curX, curY, angle)
}
