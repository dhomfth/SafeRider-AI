package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

data class UserLocationData(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Int,
    val bearing: Float,
    val accuracyMeters: Float,
    val altitudeMeters: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class RealtimeLocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    companion object {
        private const val TAG = "RealtimeLocationTracker"
        const val UPDATE_INTERVAL_MS = 2000L
        const val FASTEST_UPDATE_INTERVAL_MS = 1000L
        const val MIN_UPDATE_DISTANCE_METERS = 1.5f

        // Standard default Jakarta anchor coordinates (Jl. Medan Merdeka)
        const val DEFAULT_JAKARTA_LAT = -6.1754
        const val DEFAULT_JAKARTA_LNG = 106.8272

        fun hasLocationPermission(context: Context): Boolean {
            val fine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            return fine || coarse
        }

        fun hasFineLocationPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * One-shot location retrieval for route planning and initial positioning.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationOnce(): UserLocationData? {
        if (!hasLocationPermission(context)) return null

        try {
            // 1. Try Play Services lastLocation
            val lastLoc = fusedLocationClient.lastLocation.await()
            if (lastLoc != null) {
                return parseLocation(lastLoc)
            }

            // 2. Try Play Services current location
            val priority = if (hasFineLocationPermission(context)) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }
            val currentLoc = fusedLocationClient.getCurrentLocation(priority, null).await()
            if (currentLoc != null) {
                return parseLocation(currentLoc)
            }

            // 3. Fallback to LocationManager (GPS or Network)
            locationManager?.let { lm ->
                val gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLoc != null) return parseLocation(gpsLoc)

                val netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (netLoc != null) return parseLocation(netLoc)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching one-shot location: ${e.message}")
        }
        return null
    }

    /**
     * Real-time continuous location stream.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<UserLocationData> = callbackFlow {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted when requesting updates.")
            close()
            return@callbackFlow
        }

        val priority = if (hasFineLocationPermission(context)) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val locationRequest = LocationRequest.Builder(priority, UPDATE_INTERVAL_MS).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            setWaitForAccurateLocation(false)
        }.build()

        var lastEmittedTime = 0L

        val playServicesCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                lastEmittedTime = System.currentTimeMillis()
                trySend(parseLocation(location))
            }
        }

        // LocationManager fallback listener
        val systemLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // If Play Services hasn't emitted recently, use system location
                if (System.currentTimeMillis() - lastEmittedTime > 2500) {
                    trySend(parseLocation(location))
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Seed immediately with last known location
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    trySend(parseLocation(loc))
                } else {
                    locationManager?.let { lm ->
                        val fallback = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (fallback != null) {
                            trySend(parseLocation(fallback))
                        }
                    }
                }
            }

            // Register Fused Location Provider
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                playServicesCallback,
                Looper.getMainLooper()
            )

            // Register System Location Provider as fallback
            locationManager?.let { lm ->
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        UPDATE_INTERVAL_MS,
                        MIN_UPDATE_DISTANCE_METERS,
                        systemLocationListener,
                        Looper.getMainLooper()
                    )
                } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        UPDATE_INTERVAL_MS,
                        MIN_UPDATE_DISTANCE_METERS,
                        systemLocationListener,
                        Looper.getMainLooper()
                    )
                }
            }

            Log.d(TAG, "Real-time location updates successfully started.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException registering location updates", e)
            close(e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception registering location updates", e)
        }

        awaitClose {
            Log.d(TAG, "Cleaning up real-time location updates.")
            try {
                fusedLocationClient.removeLocationUpdates(playServicesCallback)
                locationManager?.removeUpdates(systemLocationListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error removing location updates: ${e.message}")
            }
        }
    }

    private fun parseLocation(location: Location): UserLocationData {
        val speedKmh = if (location.hasSpeed()) {
            (location.speed * 3.6f).roundToInt()
        } else {
            0
        }
        return UserLocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            speedKmh = speedKmh,
            bearing = if (location.hasBearing()) location.bearing else 0f,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else 5.0f,
            altitudeMeters = if (location.hasAltitude()) location.altitude else 12.0,
            timestamp = location.time
        )
    }
}
