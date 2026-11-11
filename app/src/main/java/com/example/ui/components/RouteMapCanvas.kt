package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.example.model.RouteOption
import com.example.ui.theme.SafeRiderAmber
import com.example.ui.theme.SafeRiderCyan
import com.example.ui.theme.SafeRiderGreen
import com.example.ui.theme.SafeRiderOrange
import com.example.ui.theme.SafeRiderRed

@Composable
fun RouteMapCanvas(
    route: RouteOption?,
    hazards: List<HazardMarker>,
    progressRatio: Float = 0f,
    isNavigating: Boolean = false,
    onHazardSelected: ((HazardMarker) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
            .background(Color(0xFF070B12))
            .testTag("route_map_canvas")
            .pointerInput(hazards) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    // Find if any hazard was tapped within 40px radius
                    val tapped = hazards.firstOrNull { hazard ->
                        val hx = hazard.x * w
                        val hy = hazard.y * h
                        val distSq = (offset.x - hx) * (offset.x - hx) + (offset.y - hy) * (offset.y - hy)
                        distSq <= 45 * 45
                    }
                    if (tapped != null) {
                        onHazardSelected?.invoke(tapped)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw subtle night grid and city blocks
            drawNightCityGrid(w, h)

            // 2. Draw secondary road network
            drawRoadNetwork(w, h)

            // 3. Draw Route Polyline
            if (route != null && route.pathPoints.isNotEmpty()) {
                drawRoutePolyline(route, w, h, isNavigating, progressRatio)
            }

            // 4. Draw Hazard Markers
            drawHazardPins(hazards, w, h)

            // 5. Draw Start & Destination
            if (route != null && route.pathPoints.isNotEmpty()) {
                val start = route.pathPoints.first()
                val end = route.pathPoints.last()
                drawLocationPin(start.x * w, start.y * h, "A", SafeRiderGreen)
                drawLocationPin(end.x * w, end.y * h, "B", SafeRiderCyan)
            }

            // 6. Draw Rider Vehicle Position with Headlight Beam
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
}

private fun DrawScope.drawNightCityGrid(w: Float, h: Float) {
    val gridColor = Color(0xFF131D2E)
    val step = 60f
    var x = 0f
    while (x < w) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1f
        )
        x += step
    }
    var y = 0f
    while (y < h) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawRoadNetwork(w: Float, h: Float) {
    val roadColor = Color(0xFF1B283D)
    val arterialRoadColor = Color(0xFF243652)

    // Base roads
    drawLine(roadColor, Offset(0f, h * 0.35f), Offset(w, h * 0.35f), strokeWidth = 8f)
    drawLine(roadColor, Offset(0f, h * 0.70f), Offset(w, h * 0.70f), strokeWidth = 8f)
    drawLine(roadColor, Offset(w * 0.30f, 0f), Offset(w * 0.30f, h), strokeWidth = 8f)
    drawLine(roadColor, Offset(w * 0.65f, 0f), Offset(w * 0.65f, h), strokeWidth = 8f)

    // Diagonal arterial
    drawLine(arterialRoadColor, Offset(w * 0.10f, h * 0.90f), Offset(w * 0.90f, h * 0.10f), strokeWidth = 14f)
    drawLine(Color(0xFF0F172A), Offset(w * 0.10f, h * 0.90f), Offset(w * 0.90f, h * 0.10f), strokeWidth = 10f)
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

    // Main line (dashed if dangerous / dark)
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

private fun DrawScope.drawHazardPins(hazards: List<HazardMarker>, w: Float, h: Float) {
    hazards.forEach { hazard ->
        val x = hazard.x * w
        val y = hazard.y * h

        val pinColor = when (hazard.type) {
            HazardType.PJU_MATI -> SafeRiderAmber
            HazardType.JALAN_BERLUBANG -> SafeRiderOrange
            HazardType.RAWAN_BEGAL -> SafeRiderRed
            HazardType.BLIND_SPOT -> Color(0xFFE040FB)
            HazardType.GENANGAN_AIR -> Color(0xFF00B0FF)
        }

        // Pulse warning for Begal or PJU mati
        drawCircle(
            color = pinColor.copy(alpha = 0.25f),
            radius = 18f,
            center = Offset(x, y)
        )

        drawCircle(
            color = pinColor,
            radius = 9f,
            center = Offset(x, y)
        )

        drawCircle(
            color = Color.White,
            radius = 4f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawLocationPin(x: Float, y: Float, label: String, color: Color) {
    drawCircle(color = color.copy(alpha = 0.3f), radius = 16f, center = Offset(x, y))
    drawCircle(color = color, radius = 10f, center = Offset(x, y))
    drawCircle(color = Color.Black, radius = 5f, center = Offset(x, y))
}

data class RiderPositionResult(val x: Float, val y: Float, val heading: Float)

private fun calculateRiderPosition(route: RouteOption, progress: Float, w: Float, h: Float): RiderPositionResult {
    val points = route.pathPoints
    if (points.isEmpty()) return RiderPositionResult(w * 0.5f, h * 0.5f, 0f)
    if (points.size == 1) return RiderPositionResult(points[0].x * w, points[0].y * h, 0f)

    val totalSegments = points.size - 1
    val scaledProgress = (progress * totalSegments).coerceIn(0f, totalSegments.toFloat())
    val currentSegment = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
    val segmentProgress = scaledProgress - currentSegment

    val p1 = points[currentSegment]
    val p2 = points[currentSegment + 1]

    val curX = (p1.x + (p2.x - p1.x) * segmentProgress) * w
    val curY = (p1.y + (p2.y - p1.y) * segmentProgress) * h

    val dx = (p2.x - p1.x) * w
    val dy = (p2.y - p1.y) * h
    val heading = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()

    return RiderPositionResult(curX, curY, heading)
}

private fun DrawScope.drawRiderMarker(
    x: Float,
    y: Float,
    heading: Float,
    pulseRadius: Float,
    pulseAlpha: Float,
    isNavigating: Boolean
) {
    // 1. Radar pulse
    drawCircle(
        color = SafeRiderCyan.copy(alpha = pulseAlpha),
        radius = pulseRadius,
        center = Offset(x, y)
    )

    // 2. Headlight cone beam (simulates bright motorcycle beam lighting up dark road)
    val beamLength = 65f
    val spreadAngle = 35.0
    val rad = Math.toRadians(heading.toDouble())
    val leftRad = Math.toRadians(heading - spreadAngle)
    val rightRad = Math.toRadians(heading + spreadAngle)

    val beamPath = Path().apply {
        moveTo(x, y)
        lineTo(
            (x + beamLength * Math.cos(leftRad)).toFloat(),
            (y + beamLength * Math.sin(leftRad)).toFloat()
        )
        lineTo(
            (x + beamLength * Math.cos(rightRad)).toFloat(),
            (y + beamLength * Math.sin(rightRad)).toFloat()
        )
        close()
    }

    drawPath(
        path = beamPath,
        brush = Brush.radialGradient(
            colors = listOf(
                SafeRiderCyan.copy(alpha = 0.5f),
                SafeRiderCyan.copy(alpha = 0.15f),
                Color.Transparent
            ),
            center = Offset(x, y),
            radius = beamLength
        )
    )

    // 3. Vehicle body dot
    drawCircle(
        color = Color(0xFF0F172A),
        radius = 12f,
        center = Offset(x, y)
    )
    drawCircle(
        color = SafeRiderCyan,
        radius = 9f,
        center = Offset(x, y)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(x, y)
    )
}
