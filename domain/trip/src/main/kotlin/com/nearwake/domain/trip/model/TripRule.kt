package com.nearwake.domain.trip.model

import kotlin.math.ceil
import kotlinx.serialization.Serializable

@Serializable
data class TripRule(
    val alertLeadMinutes: Int,
    val alertTriggerMode: AlertTriggerMode = AlertTriggerMode.TIME,
    val alertDistanceMeters: Int = DEFAULT_ALERT_DISTANCE_METERS,
    val approachRadiusMeters: Float = DEFAULT_APPROACH_RADIUS_METERS,
    val destinationRadiusMeters: Float = DEFAULT_DESTINATION_RADIUS_METERS,
    val offlineBiasPercent: Int = DEFAULT_OFFLINE_BIAS_PERCENT,
    val preciseBurstMaxSeconds: Int = DEFAULT_PRECISE_BURST_MAX_SECONDS,
    val noMotionTimeoutMinutes: Int = DEFAULT_NO_MOTION_TIMEOUT_MINUTES,
) {
    fun effectiveAlertThresholdMinutes(approachBufferMinutes: Int, confidence: Confidence): Int {
        val baseThreshold = alertLeadMinutes + approachBufferMinutes
        return if (confidence == Confidence.OFFLINE) {
            ceil(baseThreshold * (1 + offlineBiasPercent / 100.0)).toInt()
        } else {
            baseThreshold
        }
    }

    fun usesTimeTrigger(): Boolean = alertTriggerMode.usesTimeTrigger()

    fun usesDistanceTrigger(): Boolean = alertTriggerMode.usesDistanceTrigger()

    companion object {
        const val DEFAULT_ALERT_DISTANCE_METERS = 500
        const val DEFAULT_APPROACH_RADIUS_METERS = 1_500f
        const val DEFAULT_DESTINATION_RADIUS_METERS = 300f
        const val DEFAULT_OFFLINE_BIAS_PERCENT = 20
        const val DEFAULT_PRECISE_BURST_MAX_SECONDS = 90
        const val DEFAULT_NO_MOTION_TIMEOUT_MINUTES = 15
    }
}
