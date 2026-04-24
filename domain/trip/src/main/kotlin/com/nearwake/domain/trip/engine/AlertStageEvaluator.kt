package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripState
import kotlin.math.ceil

class AlertStageEvaluator {
    fun evaluate(
        currentStage: AlertStage,
        tripState: TripState,
        rule: TripRule,
        alertMode: AlertMode,
        confidence: Confidence,
        etaMinutes: Int?,
        distanceMeters: Double?,
        destinationGeofenceEntered: Boolean,
    ): AlertStage {
        val nextStage = when {
            tripState == TripState.Recovery -> AlertStage.RECOVERY
            tripState == TripState.Alerting || destinationGeofenceEntered -> AlertStage.ARRIVAL
            isArrival(distanceMeters = distanceMeters, rule = rule) -> AlertStage.ARRIVAL
            isImminent(
                alertMode = alertMode,
                confidence = confidence,
                etaMinutes = etaMinutes,
                distanceMeters = distanceMeters,
            ) -> AlertStage.IMMINENT

            isApproach(
                alertMode = alertMode,
                confidence = confidence,
                etaMinutes = etaMinutes,
                distanceMeters = distanceMeters,
            ) -> AlertStage.APPROACH

            else -> AlertStage.MONITORING
        }

        return if (nextStage.ordinal < currentStage.ordinal && nextStage != AlertStage.RECOVERY) {
            currentStage
        } else {
            nextStage
        }
    }

    private fun isArrival(
        distanceMeters: Double?,
        rule: TripRule,
    ): Boolean = distanceMeters != null && distanceMeters <= rule.destinationRadiusMeters

    private fun isApproach(
        alertMode: AlertMode,
        confidence: Confidence,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): Boolean {
        val biasMultiplier = stageBiasMultiplier(confidence = confidence, alertMode = alertMode)
        val approachEtaMinutes = ceil(APPROACH_ETA_MINUTES * biasMultiplier).toInt()
        val approachDistanceMeters = APPROACH_DISTANCE_METERS * biasMultiplier
        return (etaMinutes != null && etaMinutes <= approachEtaMinutes) ||
            (distanceMeters != null && distanceMeters <= approachDistanceMeters)
    }

    private fun isImminent(
        alertMode: AlertMode,
        confidence: Confidence,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): Boolean {
        val biasMultiplier = stageBiasMultiplier(confidence = confidence, alertMode = alertMode)
        val imminentEtaMinutes = ceil(IMMINENT_ETA_MINUTES * biasMultiplier).toInt()
        val imminentDistanceMeters = IMMINENT_DISTANCE_METERS * biasMultiplier
        return (etaMinutes != null && etaMinutes <= imminentEtaMinutes) ||
            (distanceMeters != null && distanceMeters <= imminentDistanceMeters)
    }

    private fun stageBiasMultiplier(
        confidence: Confidence,
        alertMode: AlertMode,
    ): Double = when (confidence) {
        Confidence.HIGH -> if (alertMode == AlertMode.SLEEP) 1.05 else 1.0
        Confidence.DEGRADED -> if (alertMode == AlertMode.SLEEP) 1.2 else 1.15
        Confidence.OFFLINE -> 1.25
    }

    companion object {
        private const val APPROACH_ETA_MINUTES = 5.0
        private const val IMMINENT_ETA_MINUTES = 2.0
        private const val APPROACH_DISTANCE_METERS = 500.0
        private const val IMMINENT_DISTANCE_METERS = 150.0
    }
}
