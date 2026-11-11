package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SafeRiderRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SafeRider AI", appName)
  }

  @Test
  fun `verify safeLight routes and hazards data`() {
    val routes = SafeRiderRepository.getSampleRoutes()
    val recommendedRoute = routes.firstOrNull { it.isRecommended }
    assertNotNull(recommendedRoute)
    assertEquals("route_safelight", recommendedRoute?.id)
    assertTrue((recommendedRoute?.illuminationScore ?: 0f) >= 8.5f)

    val hazards = SafeRiderRepository.getInitialHazards()
    assertTrue(hazards.isNotEmpty())
  }

  @Test
  fun `verify adaptive rerouting state and route transition`() {
    val routes = SafeRiderRepository.getSampleRoutes()
    val fastestRoute = routes.first { it.id == "route_fastest" }
    val safeLightRoute = routes.first { it.id == "route_safelight" }

    val suggestion = com.example.model.RerouteSuggestion(
      id = "test_reroute_1",
      alternativeRoute = safeLightRoute,
      reasonTitle = "Pemadaman Listrik & Bahaya Terdeteksi di Depan",
      reasonDescription = "Sektor jalan mengalami gangguan. Rute SafeLight menawarkan penerangan 9.2/10.",
      currentIllumination = fastestRoute.illuminationScore,
      proposedIllumination = safeLightRoute.illuminationScore,
      timeDifferenceMinutes = 3,
      hazardsAvoidedCount = 3,
      spokenPrompt = "Rute alternatif lebih aman via Jl. Protokol tersedia."
    )

    assertEquals(safeLightRoute.id, suggestion.alternativeRoute.id)
    assertTrue(suggestion.proposedIllumination > suggestion.currentIllumination)
    assertEquals(3, suggestion.hazardsAvoidedCount)
  }

  @Test
  fun `verify location tracker default coordinates`() {
    val defaultLat = com.example.location.RealtimeLocationTracker.DEFAULT_JAKARTA_LAT
    val defaultLng = com.example.location.RealtimeLocationTracker.DEFAULT_JAKARTA_LNG
    assertEquals(-6.1754, defaultLat, 0.0001)
    assertEquals(106.8272, defaultLng, 0.0001)
  }
}
