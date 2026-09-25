package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LiveGpsTelemetry(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val accuracyMeters: Float = 0f,
    val speedKmh: Float = 0f,
    val bearingDegrees: Float = 0f,
    val provider: String = "GPS",
    val timestamp: Long = System.currentTimeMillis(),
    val isMock: Boolean = false
)

class LiveLocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long = 3000L): Flow<LiveGpsTelemetry> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        var isFusedActive = false

        // 1. Try Google Play Services FusedLocationProviderClient first
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toTelemetry())
                }
            }
        }

        try {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                intervalMs
            ).apply {
                setMinUpdateIntervalMillis(intervalMs / 2)
                setMinUpdateDistanceMeters(2.0f) // Update if moved by 2m
                setWaitForAccurateLocation(false)
            }.build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                isFusedActive = true
            }.addOnFailureListener {
                // If Play Services fails, fallback to platform LocationManager
                startPlatformLocationUpdates(intervalMs) { loc ->
                    trySend(loc.toTelemetry())
                }
            }
        } catch (e: Exception) {
            startPlatformLocationUpdates(intervalMs) { loc ->
                trySend(loc.toTelemetry())
            }
        }

        awaitClose {
            try {
                if (isFusedActive) {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    private fun startPlatformLocationUpdates(intervalMs: Long, onLocation: (Location) -> Unit) {
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocation(location)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    intervalMs,
                    2.0f,
                    listener,
                    Looper.getMainLooper()
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    intervalMs,
                    2.0f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {}
    }

    private fun Location.toTelemetry(): LiveGpsTelemetry {
        return LiveGpsTelemetry(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitude,
            accuracyMeters = accuracy,
            speedKmh = speed * 3.6f,
            bearingDegrees = bearing,
            provider = provider ?: "GPS",
            timestamp = time,
            isMock = isFromMockProvider
        )
    }

    companion object {
        /**
         * Calculates Great-Circle distance in kilometers using the Haversine formula
         */
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        /**
         * Calculates initial bearing from point 1 to point 2 in degrees (0-360)
         */
        fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val dLon = Math.toRadians(lon2 - lon1)
            val y = sin(dLon) * cos(Math.toRadians(lat2))
            val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                    sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
            val brng = Math.toDegrees(atan2(y, x))
            return ((brng + 360) % 360).toFloat()
        }
    }
}
