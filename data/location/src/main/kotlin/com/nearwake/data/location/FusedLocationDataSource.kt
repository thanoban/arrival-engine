package com.nearwake.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.nearwake.domain.location.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Singleton
class FusedLocationDataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val activeCallbacks = linkedSetOf<LocationCallback>()

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): LatLng? =
        client.lastLocation
            .await()
            ?.let { LatLng(lat = it.latitude, lng = it.longitude) }

    @SuppressLint("MissingPermission")
    fun startBalancedUpdates(
        intervalMillis: Long = 60_000L,
        minDistanceMeters: Float = 100f,
    ): Flow<LatLng> =
        locationFlow(
            request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMillis)
                .setMinUpdateDistanceMeters(minDistanceMeters)
                .build(),
        )

    @SuppressLint("MissingPermission")
    fun startPreciseBurst(maxDurationMs: Long = 90_000L): Flow<LatLng> =
        locationFlow(
            request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
                .setMinUpdateIntervalMillis(2_500L)
                .build(),
            timeoutMs = maxDurationMs,
        )

    suspend fun stopUpdates() {
        val callbacks = activeCallbacks.toList()
        activeCallbacks.clear()
        callbacks.forEach { callback ->
            client.removeLocationUpdates(callback).await()
        }
    }

    @SuppressLint("MissingPermission")
    private fun locationFlow(
        request: LocationRequest,
        timeoutMs: Long? = null,
    ): Flow<LatLng> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    trySend(LatLng(lat = location.latitude, lng = location.longitude))
                }
            }
        }

        activeCallbacks += callback
        client.requestLocationUpdates(request, callback, Looper.getMainLooper()).await()

        val timeoutJob = timeoutMs?.let { duration ->
            launch {
                delay(duration)
                close()
            }
        }

        awaitClose {
            timeoutJob?.cancel()
            activeCallbacks.remove(callback)
            client.removeLocationUpdates(callback)
        }
    }
}
