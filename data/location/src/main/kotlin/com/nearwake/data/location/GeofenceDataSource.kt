package com.nearwake.data.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.nearwake.data.location.receivers.GeofenceBroadcastReceiver
import com.nearwake.domain.location.model.GeofenceSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class GeofenceDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_GEOFENCE,
            Intent(context, GeofenceBroadcastReceiver::class.java).setAction(
                GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT,
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun registerGeofences(specs: List<GeofenceSpec>): Result<Unit> = runCatching {
        val geofences = specs.map { spec ->
            Geofence.Builder()
                .setRequestId(spec.id)
                .setCircularRegion(spec.center.lat, spec.center.lng, spec.radiusMeters)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()
        geofencingClient.addGeofences(request, geofencePendingIntent).await()
    }

    suspend fun removeGeofences(ids: List<String>): Result<Unit> = runCatching {
        geofencingClient.removeGeofences(ids).await()
    }

    suspend fun removeAllGeofences(): Result<Unit> = runCatching {
        geofencingClient.removeGeofences(geofencePendingIntent).await()
    }

    companion object {
        private const val REQUEST_CODE_GEOFENCE = 1001
    }
}
