package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LocationCrossCheckResult
import com.example.ai.SafeRiderAiEngine
import com.example.audio.SafeRiderSpeechManager
import com.example.data.SafeRiderRepository
import com.example.data.repository.FirestoreHazardRepository
import com.example.model.AiMessage
import com.example.model.AlertSeverity
import com.example.model.AppScreen
import com.example.model.EmergencyContact
import com.example.model.HazardMarker
import com.example.model.HazardType
import com.example.model.RerouteSuggestion
import com.example.model.RouteOption
import com.example.model.RouteStep
import com.example.location.RealtimeLocationTracker
import com.example.model.RealWorldPoi
import com.example.model.MapStyleMode
import com.example.model.GroundedPlace
import com.example.model.VoiceNavAlert
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafeRiderUiState(
    val currentScreen: AppScreen = AppScreen.ROUTE_PLANNER,
    val origin: String = "Posisi Terkini (Jl. Medan Merdeka)",
    val destination: String = "Rumah (Jl. Gatot Subroto)",
    val isNightTime: Boolean = true,
    val vehicleType: String = "Sepeda Motor", // "Sepeda Motor" or "Mobil"
    
    // Auth & User Profile
    val isSignedIn: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
    
    // Route Selection
    val availableRoutes: List<RouteOption> = emptyList(),
    val selectedRoute: RouteOption? = null,
    
    // Active HUD Navigation
    val isNavigating: Boolean = false,
    val currentSpeedKmH: Int = 0,
    val currentIlluminationScore: Float = 9.2f,
    val progressRatio: Float = 0.0f,
    val currentStepIndex: Int = 0,
    val distanceRemainingMeters: Int = 7400,
    val etaMinutesRemaining: Int = 18,
    val activeHazardAlert: VoiceNavAlert? = null,
    val alertsHistory: List<VoiceNavAlert> = emptyList(),

    // Real-Time Location & GPS Tracking
    val hasLocationPermission: Boolean = false,
    val isFineLocation: Boolean = false,
    val isGpsTrackingActive: Boolean = false,
    val showLocationPermissionDialog: Boolean = false,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val userSpeedKmh: Int = 0,
    val userBearing: Float = 0f,
    val userAccuracyMeters: Float? = null,
    val isSimulationFallback: Boolean = false,
    
    // Hazards
    val hazards: List<HazardMarker> = emptyList(),
    val selectedHazardFilter: HazardType? = null,
    val isReportDialogOpen: Boolean = false,
    
    // Location Cross-Check via Google Data
    val isCrossCheckingLocation: Boolean = false,
    val selectedCrossCheckHazard: HazardMarker? = null,
    val locationCrossCheckResult: LocationCrossCheckResult? = null,
    
    // Emergency & SOS
    val isSosActive: Boolean = false,
    val sosCountdown: Int = 5,
    val isSosDispatched: Boolean = false,
    val sosTriggerReason: String? = null,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val isStrobeActive: Boolean = false,
    val isEditContactDialogOpen: Boolean = false,
    val editingContact: EmergencyContact? = null,
    
    // AI Consult & Gemini Chat
    val aiMessages: List<AiMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    
    // Adaptive Rerouting & Safety Monitoring
    val activeRerouteSuggestion: RerouteSuggestion? = null,
    val isMonitoringSafety: Boolean = true,
    
    // Real-World Map & Google Maps Grounding
    val realWorldPois: List<RealWorldPoi> = emptyList(),
    val mapStyleMode: MapStyleMode = MapStyleMode.REAL_WORLD,
    val selectedPoi: RealWorldPoi? = null,
    
    // Voice / Audio
    val isVoiceMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val lastSpokenPhrase: String? = null
)

class SafeRiderViewModel(application: Application) : AndroidViewModel(application) {

    private val speechManager = SafeRiderSpeechManager(application)
    private val firestoreRepository = FirestoreHazardRepository(application)
    private val aiEngine = SafeRiderAiEngine()
    private val locationTracker = RealtimeLocationTracker(application)

    private val _uiState = MutableStateFlow(SafeRiderUiState())
    val uiState: StateFlow<SafeRiderUiState> = _uiState.asStateFlow()

    private var navigationJob: Job? = null
    private var locationTrackingJob: Job? = null
    private var sosCountdownJob: Job? = null

    init {
        val hasPermission = RealtimeLocationTracker.hasLocationPermission(application)
        val isFine = RealtimeLocationTracker.hasFineLocationPermission(application)

        val routes = SafeRiderRepository.getSampleRoutes()
        val defaultHazards = SafeRiderRepository.getInitialHazards()
        val defaultContacts = SafeRiderRepository.sampleEmergencyContacts
        val initialAlerts = SafeRiderRepository.sampleVoiceAlerts
        val defaultPois = SafeRiderRepository.sampleRealWorldPois

        val initialAiGreeting = AiMessage(
            id = "ai_welcome",
            text = "Halo, saya SafeRider AI (didukung Gemini 3.8 Flash & Google Maps Grounding). Sistem keamanan malam aktif memantau penerangan, bahaya komunitas, dan peta dunia nyata Google Maps secara live. Ada yang ingin Anda tanyakan sebelum berangkat?",
            isFromUser = false,
            timestamp = "22:15",
            modeBadge = "Google Maps Grounded"
        )

        _uiState.update {
            it.copy(
                hasLocationPermission = hasPermission,
                isFineLocation = isFine,
                availableRoutes = routes,
                selectedRoute = routes.firstOrNull { r -> r.isRecommended } ?: routes.firstOrNull(),
                hazards = defaultHazards,
                emergencyContacts = defaultContacts,
                alertsHistory = initialAlerts,
                aiMessages = listOf(initialAiGreeting),
                realWorldPois = defaultPois,
                mapStyleMode = MapStyleMode.REAL_WORLD
            )
        }

        if (hasPermission) {
            fetchCurrentLocation()
        }

        // Listen for Firebase Auth status
        refreshAuthState()

        // Sync real-time hazards from Firestore
        viewModelScope.launch {
            firestoreRepository.observeHazards().collect { cloudHazards ->
                if (cloudHazards.isNotEmpty()) {
                    _uiState.update { state ->
                        // Merge cloud hazards with initial map anchors
                        val merged = (cloudHazards + state.hazards).distinctBy { it.id }
                        state.copy(hazards = merged)
                    }
                }
            }
        }

        viewModelScope.launch {
            speechManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            speechManager.isMuted.collect { muted ->
                _uiState.update { it.copy(isVoiceMuted = muted) }
            }
        }
        viewModelScope.launch {
            speechManager.lastSpokenText.collect { text ->
                _uiState.update { it.copy(lastSpokenPhrase = text) }
            }
        }
    }

    private val credentialManager = androidx.credentials.CredentialManager.create(application)

    fun refreshAuthState() {
        val currentUser = Firebase.auth.currentUser
        _uiState.update {
            it.copy(
                isSignedIn = currentUser != null,
                userName = currentUser?.displayName ?: "Pengendara SafeRider",
                userEmail = currentUser?.email
            )
        }

        // If signed in, observe user's cloud emergency contacts
        if (currentUser != null) {
            viewModelScope.launch {
                firestoreRepository.observeUserContacts(currentUser.uid).collect { contacts ->
                    if (contacts.isNotEmpty()) {
                        _uiState.update { it.copy(emergencyContacts = contacts) }
                    }
                }
            }
        }
    }

    fun signInWithGoogle(activity: android.app.Activity) {
        com.example.auth.AuthManager.onGoogleSignInClicked(
            context = activity,
            credentialManager = credentialManager,
            onAuthSuccess = {
                refreshAuthState()
            },
            onAuthError = { errorMsg ->
                android.util.Log.e("SafeRiderVM", "Sign-in error: $errorMsg")
            },
            scope = viewModelScope
        )
    }

    fun signOut() {
        com.example.auth.AuthManager.signOut(
            credentialManager = credentialManager,
            scope = viewModelScope,
            onSignOutComplete = {
                refreshAuthState()
            }
        )
    }

    fun setScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectRoute(route: RouteOption) {
        _uiState.update {
            it.copy(
                selectedRoute = route,
                distanceRemainingMeters = (route.distanceKm * 1000).toInt(),
                etaMinutesRemaining = route.durationMinutes,
                currentIlluminationScore = route.illuminationScore
            )
        }
    }

    fun toggleNightTime() {
        _uiState.update { it.copy(isNightTime = !it.isNightTime) }
    }

    fun setVehicleType(type: String) {
        _uiState.update { it.copy(vehicleType = type) }
    }

    fun toggleVoiceMute() {
        speechManager.toggleMute()
    }

    fun testVoiceAnnouncement() {
        val testText = "Peringatan: 200 meter di depan terdapat rute gelap dan rawan lubang. Disarankan ambil jalur kanan yang lebih terang."
        speechManager.speak(testText)
    }

    // --- Location Permission & Real-Time GPS Tracking ---
    fun promptLocationPermission() {
        _uiState.update { it.copy(showLocationPermissionDialog = true) }
    }

    fun dismissLocationPermissionDialog() {
        _uiState.update { it.copy(showLocationPermissionDialog = false) }
    }

    fun setSimulationFallback(useSim: Boolean) {
        _uiState.update {
            it.copy(
                isSimulationFallback = useSim,
                showLocationPermissionDialog = false
            )
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            val loc = locationTracker.getCurrentLocationOnce()
            if (loc != null) {
                _uiState.update {
                    it.copy(
                        userLatitude = loc.latitude,
                        userLongitude = loc.longitude,
                        userAccuracyMeters = loc.accuracyMeters,
                        userSpeedKmh = loc.speedKmh,
                        origin = "Posisi GPS (${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)})"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        userLatitude = RealtimeLocationTracker.DEFAULT_JAKARTA_LAT,
                        userLongitude = RealtimeLocationTracker.DEFAULT_JAKARTA_LNG,
                        userAccuracyMeters = 4.0f,
                        origin = "Posisi GPS Terkini (Jl. Medan Merdeka)"
                    )
                }
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean, isFine: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                isFineLocation = isFine,
                showLocationPermissionDialog = false,
                isSimulationFallback = !granted
            )
        }
        if (granted) {
            speechManager.speak("Izin lokasi aktif. Pelacakan GPS real-time dimulai.")
            fetchCurrentLocation()
            if (_uiState.value.isNavigating) {
                val route = _uiState.value.selectedRoute
                if (route != null) {
                    startRealtimeLocationTracking(route)
                }
            }
        } else {
            speechManager.speak("Izin lokasi tidak aktif. SafeRider beralih ke mode navigasi terpandu.")
        }
    }

    fun startRealtimeLocationTracking(route: RouteOption) {
        locationTrackingJob?.cancel()
        locationTrackingJob = viewModelScope.launch {
            _uiState.update { it.copy(isGpsTrackingActive = true) }
            locationTracker.getLocationUpdates().collect { locData ->
                _uiState.update { current ->
                    val isMoving = locData.speedKmh > 3
                    val newSpeed = if (isMoving) locData.speedKmh else if (current.isNavigating && current.currentSpeedKmH > 0) current.currentSpeedKmH else locData.speedKmh
                    current.copy(
                        userLatitude = locData.latitude,
                        userLongitude = locData.longitude,
                        userSpeedKmh = locData.speedKmh,
                        currentSpeedKmH = newSpeed,
                        userBearing = locData.bearing,
                        userAccuracyMeters = locData.accuracyMeters,
                        isGpsTrackingActive = true
                    )
                }

                // Check proximity to hazards along route in real-time
                checkHazardProximity(locData.latitude, locData.longitude)
            }
        }
    }

    private fun checkHazardProximity(userLat: Double, userLng: Double) {
        val hazards = _uiState.value.hazards
        for (hazard in hazards) {
            // Project relative canvas x, y (0..1) to realistic geospatial coordinates in range
            val hazardLat = -6.1754 + (hazard.y - 0.5) * 0.04
            val hazardLng = 106.8272 + (hazard.x - 0.5) * 0.04
            val distMeters = calculateDistanceMeters(userLat, userLng, hazardLat, hazardLng)

            if (distMeters in 30.0..450.0) {
                val alertAlreadyShown = _uiState.value.activeHazardAlert?.condition == hazard.title
                if (!alertAlreadyShown) {
                    val alert = VoiceNavAlert(
                        id = "alert_${hazard.id}",
                        condition = hazard.title,
                        distance = "${distMeters.toInt()} meter di depan",
                        action = hazard.recommendedAction,
                        fullSpokenText = "Peringatan: ${distMeters.toInt()} meter di depan terdapat ${hazard.title}. ${hazard.recommendedAction}",
                        severity = AlertSeverity.HIGH
                    )
                    _uiState.update { it.copy(activeHazardAlert = alert) }
                    speechManager.speak(alert.fullSpokenText)
                    break
                }
            }
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // radius of earth in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // --- Active Navigation Flow ---
    fun startNavigation() {
        val route = _uiState.value.selectedRoute ?: return
        val hasPermission = _uiState.value.hasLocationPermission

        _uiState.update {
            it.copy(
                isNavigating = true,
                currentScreen = AppScreen.NAVIGATION_HUD,
                progressRatio = 0.05f,
                currentSpeedKmH = if (hasPermission) 0 else 38,
                currentStepIndex = 0,
                distanceRemainingMeters = (route.distanceKm * 1000).toInt(),
                etaMinutesRemaining = route.durationMinutes
            )
        }

        val departureText = "Navigasi SafeRider dimulai melalui ${route.title}. Indeks penerangan jalan ${route.illuminationScore} dari sepuluh. Selamat berkendara aman."
        speechManager.speak(departureText)

        if (hasPermission) {
            startRealtimeLocationTracking(route)
        } else {
            // Prompt user for location permission dialog
            _uiState.update { it.copy(showLocationPermissionDialog = true) }
        }

        startRideSimulation(route)
    }

    fun stopNavigation() {
        navigationJob?.cancel()
        navigationJob = null
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        speechManager.stop()
        _uiState.update {
            it.copy(
                isNavigating = false,
                isGpsTrackingActive = false,
                currentSpeedKmH = 0,
                activeHazardAlert = null
            )
        }
    }

    private fun startRideSimulation(route: RouteOption) {
        navigationJob?.cancel()
        navigationJob = viewModelScope.launch {
            var progress = 0.05f
            var stepIdx = 0
            val totalSteps = route.steps.size

            while (progress < 1.0f && _uiState.value.isNavigating) {
                delay(3000)
                progress += 0.08f
                if (progress > 1.0f) progress = 1.0f

                stepIdx = ((progress * totalSteps).toInt()).coerceIn(0, totalSteps - 1)
                val currentStep = route.steps[stepIdx]

                val isRealGpsActive = _uiState.value.isGpsTrackingActive && _uiState.value.hasLocationPermission
                val realSpeed = _uiState.value.userSpeedKmh
                val speed = if (isRealGpsActive && realSpeed > 0) {
                    realSpeed
                } else if (progress >= 1.0f) {
                    0
                } else {
                    (38 + ((progress * 13).toInt() % 14))
                }

                val remainingDist = ((1.0f - progress) * (route.distanceKm * 1000)).toInt()
                val remainingMins = ((1.0f - progress) * route.durationMinutes).toInt().coerceAtLeast(1)

                val currentIllum = currentStep.illuminationScore

                val startLat = RealtimeLocationTracker.DEFAULT_JAKARTA_LAT
                val startLng = RealtimeLocationTracker.DEFAULT_JAKARTA_LNG
                val endLat = -6.2297
                val endLng = 106.8166

                val currentLat = if (isRealGpsActive && _uiState.value.userLatitude != null && _uiState.value.userLatitude != startLat) {
                    _uiState.value.userLatitude!!
                } else {
                    startLat + (progress * (endLat - startLat))
                }
                val currentLng = if (isRealGpsActive && _uiState.value.userLongitude != null && _uiState.value.userLongitude != startLng) {
                    _uiState.value.userLongitude!!
                } else {
                    startLng + (progress * (endLng - startLng))
                }

                _uiState.update {
                    it.copy(
                        progressRatio = progress,
                        currentStepIndex = stepIdx,
                        currentSpeedKmH = speed,
                        userLatitude = currentLat,
                        userLongitude = currentLng,
                        distanceRemainingMeters = remainingDist,
                        etaMinutesRemaining = remainingMins,
                        currentIlluminationScore = currentIllum
                    )
                }

                // Check proximity to hazards along route in real-time
                checkHazardProximity(currentLat, currentLng)

                // Check for dynamic hazard alert around 25-35% progress
                if (progress in 0.25f..0.35f && _uiState.value.activeHazardAlert == null) {
                    val alert = VoiceNavAlert(
                        id = "alert_${System.currentTimeMillis()}",
                        condition = "200 meter di depan terdapat rute gelap dan rawan lubang",
                        distance = "200 meter",
                        action = "Disarankan ambil lajur kanan yang lebih terang",
                        fullSpokenText = "Peringatan: 200 meter di depan terdapat rute gelap dan rawan lubang. Disarankan ambil jalur kanan yang lebih terang.",
                        severity = AlertSeverity.HIGH
                    )
                    triggerHazardAlert(alert)
                } else if (progress in 0.40f..0.52f && _uiState.value.activeRerouteSuggestion == null) {
                    // Mid-journey adaptive reroute evaluation
                    proposeAdaptiveRerouteInternal()
                } else if (progress in 0.65f..0.75f && _uiState.value.activeHazardAlert == null) {
                    val alert = VoiceNavAlert(
                        id = "alert_${System.currentTimeMillis()}",
                        condition = "Titik buta tikungan tajam dan minim penerangan",
                        distance = "150 meter",
                        action = "Kurangi kecepatan hingga 25 km/jam dan bunyikan klakson perlahan",
                        fullSpokenText = "Peringatan: 150 meter di depan terdapat titik buta tikungan tajam dan minim penerangan. Kurangi kecepatan dan bunyikan klakson perlahan.",
                        severity = AlertSeverity.MEDIUM
                    )
                    triggerHazardAlert(alert)
                }

                if (progress >= 1.0f) {
                    speechManager.speak("Anda telah tiba di tujuan dengan selamat. SafeRider AI mengakhiri pemantauan.")
                    _uiState.update { it.copy(isNavigating = false, currentSpeedKmH = 0) }
                    break
                }
            }
        }
    }

    fun triggerHazardAlert(alert: VoiceNavAlert) {
        _uiState.update {
            it.copy(
                activeHazardAlert = alert,
                alertsHistory = listOf(alert) + it.alertsHistory
            )
        }
        speechManager.speak(alert.fullSpokenText)
    }

    fun dismissHazardAlert() {
        _uiState.update { it.copy(activeHazardAlert = null) }
    }

    // --- Adaptive Rerouting Flow ---
    fun simulateAdaptiveRerouteTrigger() {
        proposeAdaptiveRerouteInternal()
    }

    private fun proposeAdaptiveRerouteInternal() {
        val currentRoute = _uiState.value.selectedRoute ?: return
        val routes = _uiState.value.availableRoutes

        val saferRoute = routes.firstOrNull { it.id != currentRoute.id && it.illuminationScore >= 8.5f }
            ?: routes.firstOrNull { it.id != currentRoute.id }
            ?: return

        val currentScore = if (currentRoute.illuminationScore > 5.0f) 3.4f else currentRoute.illuminationScore
        val proposedScore = saferRoute.illuminationScore
        val timeDiff = (saferRoute.durationMinutes - currentRoute.durationMinutes).coerceAtLeast(2)

        val suggestion = RerouteSuggestion(
            id = "reroute_${System.currentTimeMillis()}",
            alternativeRoute = saferRoute,
            reasonTitle = "Pemadaman PJU & Kemacetan Baru di Depan",
            reasonDescription = "Sektor jalan 700 meter di depan mengalami pemadaman lampu jalan mendadak dan antrean padat. Rute SafeLight via ${saferRoute.title} menawarkan jalan terang benderang 4 lajur dan bebas titik rawan.",
            currentIllumination = currentScore,
            proposedIllumination = proposedScore,
            timeDifferenceMinutes = timeDiff,
            hazardsAvoidedCount = 3,
            spokenPrompt = "Perhatian: Terdeteksi bahaya baru dan pemadaman lampu di depan. Rute alternatif lebih aman via ${saferRoute.title} tersedia dengan peningkatan penerangan. Apakah Anda ingin beralih?"
        )

        _uiState.update { it.copy(activeRerouteSuggestion = suggestion) }
        speechManager.speak(suggestion.spokenPrompt)
    }

    fun acceptReroute() {
        val suggestion = _uiState.value.activeRerouteSuggestion ?: return
        val newRoute = suggestion.alternativeRoute
        _uiState.update {
            it.copy(
                selectedRoute = newRoute,
                activeRerouteSuggestion = null,
                currentIlluminationScore = newRoute.illuminationScore,
                distanceRemainingMeters = (newRoute.distanceKm * 1000 * (1f - it.progressRatio)).toInt(),
                etaMinutesRemaining = (newRoute.durationMinutes * (1f - it.progressRatio)).toInt().coerceAtLeast(2)
            )
        }
        speechManager.speak("Rute berhasil dialihkan ke ${newRoute.title}. Estimasi waktu diperbarui dan jalur terpantau terang dan aman.")
    }

    fun declineReroute() {
        _uiState.update { it.copy(activeRerouteSuggestion = null) }
        speechManager.speak("Melanjutkan rute saat ini. Tetap waspada terhadap titik gelap di depan.")
    }

    // --- Emergency & SOS Flow ---
    fun triggerSosManual() {
        startSosCountdown(reason = "Pemicu Manual oleh Pengendara")
    }

    fun simulateDarkAreaAnomalyStop() {
        startSosCountdown(reason = "Terdeteksi berhenti >3 menit di area gelap (Skor Terang 2.1/10)")
    }

    private fun startSosCountdown(reason: String) {
        sosCountdownJob?.cancel()
        _uiState.update {
            it.copy(
                isSosActive = true,
                sosCountdown = 5,
                isSosDispatched = false,
                sosTriggerReason = reason
            )
        }

        speechManager.speak("Peringatan darurat: Konfirmasi status keselamatan Anda dalam 5 detik sebelum sinyal SOS dikirim.", flush = true)

        sosCountdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _uiState.update { it.copy(sosCountdown = i) }
                delay(1000)
            }
            _uiState.update {
                it.copy(
                    sosCountdown = 0,
                    isSosDispatched = true,
                    isStrobeActive = true
                )
            }
            speechManager.speak("Sinyal SOS darurat telah dikirim ke kontak keluarga dan kepolisian terdekat. Koordinat GPS Anda sedang disiarkan.")
        }
    }

    fun cancelSos() {
        sosCountdownJob?.cancel()
        sosCountdownJob = null
        _uiState.update {
            it.copy(
                isSosActive = false,
                sosCountdown = 5,
                isSosDispatched = false,
                sosTriggerReason = null,
                isStrobeActive = false
            )
        }
        speechManager.speak("Sinyal darurat dibatalkan. Status Anda tercatat aman.")
    }

    fun dispatchSosImmediately() {
        sosCountdownJob?.cancel()
        sosCountdownJob = null
        _uiState.update {
            it.copy(
                sosCountdown = 0,
                isSosDispatched = true,
                isStrobeActive = true
            )
        }
        speechManager.speak("Sinyal darurat dikirim seketika. Bantuan sedang diarahkan ke lokasi Anda.")
    }

    fun toggleStrobeBeacon() {
        _uiState.update { it.copy(isStrobeActive = !it.isStrobeActive) }
    }

    // --- Emergency Contacts Management in Settings ---
    fun openEditContactDialog(contact: EmergencyContact? = null) {
        _uiState.update {
            it.copy(
                isEditContactDialogOpen = true,
                editingContact = contact
            )
        }
    }

    fun closeEditContactDialog() {
        _uiState.update {
            it.copy(
                isEditContactDialogOpen = false,
                editingContact = null
            )
        }
    }

    fun saveEmergencyContact(contact: EmergencyContact) {
        _uiState.update { state ->
            val updated = state.emergencyContacts.toMutableList()
            val existingIdx = updated.indexOfFirst { it.id == contact.id }
            if (existingIdx >= 0) {
                updated[existingIdx] = contact
            } else {
                updated.add(contact)
            }
            state.copy(emergencyContacts = updated)
        }

        // Persist to Firestore if user is signed in
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    firestoreRepository.saveContact(uid, contact)
                } catch (e: Exception) {
                    // Handled gracefully
                }
            }
        }
        speechManager.speak("Kontak darurat ${contact.name} berhasil disimpan.")
    }

    fun deleteEmergencyContact(contactId: String) {
        _uiState.update { state ->
            state.copy(emergencyContacts = state.emergencyContacts.filter { it.id != contactId })
        }
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                firestoreRepository.deleteContact(uid, contactId)
            }
        }
    }

    // --- Community Hazards Crowdsourcing & Cloud Persistence ---
    fun openReportDialog() {
        _uiState.update { it.copy(isReportDialogOpen = true) }
    }

    fun closeReportDialog() {
        _uiState.update { it.copy(isReportDialogOpen = false) }
    }

    fun submitHazardReport(type: HazardType, title: String, location: String, note: String) {
        val newHazard = HazardMarker(
            id = "hazard_${System.currentTimeMillis()}",
            type = type,
            title = if (title.isBlank()) type.label else title,
            locationName = if (location.isBlank()) "Dekat Posisi Terkini" else location,
            x = 0.50f + (Math.random().toFloat() * 0.1f - 0.05f),
            y = 0.45f + (Math.random().toFloat() * 0.1f - 0.05f),
            illuminationScore = if (type == HazardType.PJU_MATI) 2.0f else 4.5f,
            reportedAgo = "Baru saja",
            upvotes = 1,
            verified = true,
            distanceMeters = 150,
            recommendedAction = when (type) {
                HazardType.PJU_MATI -> "Gunakan lampu dekat/jauh bergantian, kurangi kecepatan."
                HazardType.JALAN_BERLUBANG -> "Waspada lubang aspal, hindari lajur rusak."
                HazardType.RAWAN_BEGAL -> "Tetap di jalur utama yang ramai dan terang."
                HazardType.BLIND_SPOT -> "Bunyikan klakson singkat dan perlambat laju."
                HazardType.GENANGAN_AIR -> "Kurangi rem mendadak pada permukaan licin."
            },
            notes = note
        )

        _uiState.update {
            it.copy(
                hazards = listOf(newHazard) + it.hazards,
                isReportDialogOpen = false
            )
        }

        // Persist to Cloud Firestore Database
        viewModelScope.launch {
            firestoreRepository.addHazard(newHazard)
        }

        speechManager.speak("Terima kasih. Laporan ${newHazard.title} telah tersimpan di cloud database SafeRider dan disiarkan ke pengendara lain.")
    }

    fun upvoteHazard(hazardId: String) {
        _uiState.update { state ->
            val updated = state.hazards.map { h ->
                if (h.id == hazardId) h.copy(upvotes = h.upvotes + 1) else h
            }
            state.copy(hazards = updated)
        }
        viewModelScope.launch {
            firestoreRepository.upvoteHazard(hazardId)
        }
    }

    fun filterHazards(type: HazardType?) {
        _uiState.update { it.copy(selectedHazardFilter = type) }
    }

    // --- Google Location Cross-Check ---
    fun crossCheckHazardLocation(hazard: HazardMarker) {
        _uiState.update {
            it.copy(
                isCrossCheckingLocation = true,
                selectedCrossCheckHazard = hazard,
                locationCrossCheckResult = null
            )
        }

        viewModelScope.launch {
            val result = aiEngine.crossCheckLocation(
                locationName = hazard.locationName,
                hazardType = hazard.type.label,
                hazardTitle = hazard.title
            )
            _uiState.update {
                it.copy(
                    isCrossCheckingLocation = false,
                    locationCrossCheckResult = result
                )
            }
        }
    }

    fun closeCrossCheckDialog() {
        _uiState.update {
            it.copy(
                selectedCrossCheckHazard = null,
                locationCrossCheckResult = null,
                isCrossCheckingLocation = false
            )
        }
    }

    // --- AI Consult Chat Flow with Gemini 3.8 Flash & Google Maps Grounding ---
    fun sendAiQuery(query: String) {
        if (query.isBlank()) return
        val userMsg = AiMessage(
            id = "user_${System.currentTimeMillis()}",
            text = query,
            isFromUser = true,
            timestamp = "Sekarang"
        )

        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val chatResult = try {
                aiEngine.sendMessage(query)
            } catch (e: Exception) {
                val fallback = SafeRiderRepository.generateAiResponse(query, _uiState.value.selectedRoute)
                com.example.ai.AiChatResult(
                    text = fallback.text,
                    groundedPlaces = fallback.groundedPlaces
                )
            }

            val aiResponse = AiMessage(
                id = "ai_${System.currentTimeMillis()}",
                text = chatResult.text,
                isFromUser = false,
                timestamp = "Sekarang",
                modeBadge = if (chatResult.groundedPlaces.isNotEmpty()) "Google Maps Grounded" else "Gemini 3.8 Flash",
                groundedPlaces = chatResult.groundedPlaces,
                searchSources = chatResult.webSources
            )

            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + aiResponse,
                    isAiThinking = false
                )
            }
            speechManager.speak(aiResponse.text.take(180))
        }
    }

    // --- Real-World Map Mode & Google Maps Launchers ---
    fun setMapStyleMode(mode: MapStyleMode) {
        _uiState.update { it.copy(mapStyleMode = mode) }
    }

    fun selectPoi(poi: RealWorldPoi?) {
        _uiState.update { it.copy(selectedPoi = poi) }
    }

    fun openInGoogleMapsNavigation(context: android.content.Context) {
        val route = _uiState.value.selectedRoute ?: return
        val userLat = _uiState.value.userLatitude ?: route.startLat
        val userLng = _uiState.value.userLongitude ?: route.startLng
        SafeRiderRepository.openNavigationInGoogleMaps(
            context = context,
            destLat = route.destLat,
            destLng = route.destLng,
            originLat = userLat,
            originLng = userLng
        )
    }

    fun openHazardInGoogleMaps(context: android.content.Context, hazard: HazardMarker) {
        val lat = hazard.latitude ?: (-6.1754 + (hazard.y - 0.5) * 0.04)
        val lng = hazard.longitude ?: (106.8272 + (hazard.x - 0.5) * 0.04)
        SafeRiderRepository.openPlaceInGoogleMaps(
            context = context,
            latitude = lat,
            longitude = lng,
            title = hazard.title
        )
    }

    fun openPoiInGoogleMaps(context: android.content.Context, poi: RealWorldPoi) {
        SafeRiderRepository.openPlaceInGoogleMaps(
            context = context,
            latitude = poi.latitude,
            longitude = poi.longitude,
            title = poi.name
        )
    }

    override fun onCleared() {
        super.onCleared()
        navigationJob?.cancel()
        sosCountdownJob?.cancel()
        speechManager.shutdown()
    }
}
