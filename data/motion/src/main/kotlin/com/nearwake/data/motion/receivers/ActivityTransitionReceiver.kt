package com.nearwake.data.motion.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.nearwake.domain.location.model.MotionState
import com.nearwake.domain.location.model.MotionType
import kotlinx.datetime.Clock
import timber.log.Timber

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) {
            Timber.w("Activity transition intent missing result")
            return
        }

        val result = ActivityTransitionResult.extractResult(intent) ?: return
        result.transitionEvents.forEach { event ->
            ActivityTransitionEventBus.emit(
                MotionState(
                    type = event.activityType.toMotionType(),
                    confidence = event.transitionType.toConfidence(),
                    detectedAt = Clock.System.now(),
                ),
            )
        }
    }

    private fun Int.toMotionType(): MotionType =
        when (this) {
            DetectedActivity.IN_VEHICLE -> MotionType.IN_VEHICLE
            DetectedActivity.ON_BICYCLE -> MotionType.ON_BICYCLE
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING -> MotionType.ON_FOOT

            DetectedActivity.STILL -> MotionType.STILL
            else -> MotionType.UNKNOWN
        }

    private fun Int.toConfidence(): Int =
        when (this) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> 100
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> 60
            else -> 50
        }

    companion object {
        const val ACTION_ACTIVITY_TRANSITION = "com.nearwake.data.motion.ACTION_ACTIVITY_TRANSITION"
    }
}
