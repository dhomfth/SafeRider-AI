package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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

    companion object {
        private const val TAG = "RealtimeLocationTracker"
        const val UPDATE_INTERVAL_MS = 2000L
        const val FASTEST_UPDATE_INTERVAL_MS = 1000L
        const val MIN_UPDATE_DISTANCE_METERS = 2.0f

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

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val speedKmh = if (location.hasSpeed()) {
                    (location.speed * 3.6f).roundToInt()
                } else {
                    0
                }

                val data = UserLocationData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speedKmh = speedKmh,
                    bearing = location.bearing,
                    accuracyMeters = location.accuracy,
                    altitudeMeters = location.altitude,
                    timestamp = location.time
                )
                trySend(data)
            }
        }

        try {
            // Seed with last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    val speed = if (loc.hasSpeed()) (loc.speed * 3.6f).roundToInt() else 0
                    trySend(
                        UserLocationData(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            speedKmh = speed,
                            bearing = loc.bearing,
                            accuracyMeters = loc.accuracy,
                            altitudeMeters = loc.altitude,
                            timestamp = loc.time
                        )
                    )
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Real-time location updates successfully registered with priority $priority.")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException registering location updates", e)
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Removing real-time location updates.")
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
