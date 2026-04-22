package com.nearwake.data.location.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            val errorCode = event.errorCode
            if (errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
                Timber.w("Geofence service unavailable, caller should fall back to balanced FLP.")
            } else {
                Timber.w("Geofence event error: %s", GeofenceStatusCodes.getStatusCodeString(errorCode))
            }
            GeofenceEventBus.emit(
                GeofenceTransitionEvent(
                    geofenceIds = emptyList(),
                    transitionType = -1,
                    errorCode = errorCode,
                ),
            )
            return
        }

        GeofenceEventBus.emit(
            GeofenceTransitionEvent(
                geofenceIds = event.triggeringGeofences?.map { it.requestId }.orEmpty(),
                transitionType = event.geofenceTransition,
            ),
        )
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.nearwake.data.location.ACTION_GEOFENCE_EVENT"
    }
}
