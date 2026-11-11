package com.example.model

enum class HazardType(val label: String, val iconName: String) {
    PJU_MATI("Lampu PJU Mati", "flashlight_off"),
    JALAN_BERLUBANG("Jalan Rusak / Berlubang", "warning"),
    RAWAN_BEGAL("Area Rawan Begal / Kejahatan", "shield"),
    BLIND_SPOT("Titik Buta / Tikungan Gelap", "visibility_off"),
    GENANGAN_AIR("Genangan Air / Licin", "water_drop")
}

enum class AlertSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    INFO
}

data class MapPoint(
    val x: Float, // relative 0.0 - 1.0 on canvas
    val y: Float, // relative 0.0 - 1.0 on canvas
    val illuminationScore: Float, // 1.0 - 10.0
    val streetName: String
)

data class RouteStep(
    val instruction: String,
    val distanceMeters: Int,
    val streetName: String,
    val illuminationScore: Float,
    val maneuverIcon: String
)

data class RouteOption(
    val id: String,
    val title: String,
    val routeType: String, // "SAFELIGHT", "FASTEST", "ALTERNATIVE"
    val subtitle: String,
    val durationMinutes: Int,
    val distanceKm: Float,
    val illuminationScore: Float, // 1.0 - 10.0
    val safetyIndexPercent: Int, // 0 - 100
    val hazardCount: Int,
    val pjuCoveragePercent: Int,
    val streetWidth: String,
    val patrolInfo: String,
    val isRecommended: Boolean,
    val aiReasoning: String,
    val pathPoints: List<MapPoint>,
    val steps: List<RouteStep>
)

data class HazardMarker(
    val id: String,
    val type: HazardType,
    val title: String,
    val locationName: String,
    val x: Float, // Map position
    val y: Float,
    val illuminationScore: Float,
    val reportedAgo: String,
    val upvotes: Int,
    val verified: Boolean,
    val distanceMeters: Int,
    val recommendedAction: String,
    val notes: String = ""
)

data class VoiceNavAlert(
    val id: String,
    val condition: String,
    val distance: String,
    val action: String,
    val fullSpokenText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: AlertSeverity = AlertSeverity.HIGH
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val role: String,
    val phoneNumber: String,
    val isPrimary: Boolean
)

data class AiMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val modeBadge: String? = null // "VoiceNav", "RoutePlanner", "Emergency"
)

data class RerouteSuggestion(
    val id: String,
    val alternativeRoute: RouteOption,
    val reasonTitle: String,
    val reasonDescription: String,
    val currentIllumination: Float,
    val proposedIllumination: Float,
    val timeDifferenceMinutes: Int,
    val hazardsAvoidedCount: Int,
    val spokenPrompt: String,
    val trafficCondition: String = "Lancar & Terang Benderang"
)

enum class AppScreen {
    ROUTE_PLANNER,
    NAVIGATION_HUD,
    HAZARDS_MAP,
    AI_ASSISTANT,
    SETTINGS
}
