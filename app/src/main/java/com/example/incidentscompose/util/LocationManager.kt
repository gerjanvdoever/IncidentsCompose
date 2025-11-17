package com.example.incidentscompose.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationManager {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets the current location. First tries lastLocation (fast), then requests fresh location if needed.
     */
    suspend fun getCurrentLocation(context: Context): Result<Pair<Double, Double>> {
        if (!hasLocationPermission(context)) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return try {
            // First try to get last known location (fast)
            val lastLocation = getLastLocation(fusedLocationClient)
            if (lastLocation != null) {
                return Result.success(lastLocation.latitude to lastLocation.longitude)
            }

            // If no last location, request fresh location
            val freshLocation = requestFreshLocation(fusedLocationClient)
            Result.success(freshLocation.latitude to freshLocation.longitude)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Suppress("MissingPermission")
    private suspend fun getLastLocation(client: FusedLocationProviderClient): Location? {
        return suspendCancellableCoroutine { continuation ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    continuation.resume(null)
                }
        }
    }

    @Suppress("MissingPermission")
    private suspend fun requestFreshLocation(client: FusedLocationProviderClient): Location {
        return suspendCancellableCoroutine { continuation ->
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000L // 10 seconds
            ).apply {
                setMinUpdateIntervalMillis(5000L)
                setMaxUpdateDelayMillis(15000L)
                setWaitForAccurateLocation(false)
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null && continuation.isActive) {
                        client.removeLocationUpdates(this)
                        continuation.resume(location)
                    }
                }
            }

            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                client.removeLocationUpdates(locationCallback)
            }

            // Set a timeout
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                if (continuation.isActive) {
                    client.removeLocationUpdates(locationCallback)
                    continuation.resumeWithException(
                        Exception("Location request timed out. Please ensure GPS is enabled.")
                    )
                }
            }, 15000L) // 15 second timeout
        }
    }

    /**
     * Observes location updates as a Flow
     */
    @Suppress("MissingPermission")
    fun observeLocationUpdates(
        context: Context,
        interval: Long = 10000L
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission(context)) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            interval
        ).apply {
            setMinUpdateIntervalMillis(interval / 2)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}