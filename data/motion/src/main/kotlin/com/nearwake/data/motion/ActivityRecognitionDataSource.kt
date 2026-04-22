package com.nearwake.data.motion

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.nearwake.data.motion.receivers.ActivityTransitionReceiver
import com.nearwake.domain.location.model.MotionState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

@Singleton
class ActivityRecognitionDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val client = ActivityRecognition.getClient(context)

    fun observeMotionState(): Flow<MotionState> =
        com.nearwake.data.motion.receivers.ActivityTransitionEventBus.events

    @SuppressLint("MissingPermission")
    suspend fun registerVehicleTransitions() {
        client.requestActivityTransitionUpdates(
            ActivityTransitionRequest(
                listOf(
                    transition(ActivityTransition.ACTIVITY_TRANSITION_ENTER),
                    transition(ActivityTransition.ACTIVITY_TRANSITION_EXIT),
                ),
            ),
            pendingIntent,
        ).await()
    }

    suspend fun unregisterActivityTransitions() {
        client.removeActivityTransitionUpdates(pendingIntent).await()
    }

    private fun transition(transitionType: Int): ActivityTransition =
        ActivityTransition.Builder()
            .setActivityType(com.google.android.gms.location.DetectedActivity.IN_VEHICLE)
            .setActivityTransition(transitionType)
            .build()

    private val pendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ACTIVITY_TRANSITION,
            Intent(context, ActivityTransitionReceiver::class.java).setAction(
                ActivityTransitionReceiver.ACTION_ACTIVITY_TRANSITION,
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    companion object {
        private const val REQUEST_CODE_ACTIVITY_TRANSITION = 1002
    }
}
