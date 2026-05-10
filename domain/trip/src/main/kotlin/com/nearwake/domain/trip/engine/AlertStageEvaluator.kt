package com.nearwake.domain.trip.engine

import com.nearwake.core.remoteconfig.ThresholdConfig
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripState
import kotlin.math.ceil

class AlertStageEvaluator(
    private val thresholdConfig: ThresholdConfig = ThresholdConfig(),
) {
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
                rule = rule,
                etaMinutes = etaMinutes,
                distanceMeters = distanceMeters,
            ) -> AlertStage.IMMINENT

            isApproach(
                alertMode = alertMode,
                confidence = confidence,
                rule = rule,
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
        rule: TripRule,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): Boolean {
        val biasMultiplier = stageBiasMultiplier(confidence = confidence, alertMode = alertMode)
        val approachEtaMinutes = ceil(rule.alertLeadMinutes * biasMultiplier).toInt()
        val approachDistanceMeters = rule.alertDistanceMeters * biasMultiplier
        val triggeredByEta = rule.usesTimeTrigger() &&
            etaMinutes != null &&
            approachEtaMinutes > 0 &&
            etaMinutes <= approachEtaMinutes
        val triggeredByDistance = rule.usesDistanceTrigger() &&
            distanceMeters != null &&
            distanceMeters <= approachDistanceMeters
        return triggeredByEta || triggeredByDistance
    }

    private fun isImminent(
        alertMode: AlertMode,
        confidence: Confidence,
        rule: TripRule,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): Boolean {
        val biasMultiplier = stageBiasMultiplier(confidence = confidence, alertMode = alertMode)
        val imminentEtaMinutes = ceil(thresholdConfig.imminentEtaMinutes * biasMultiplier).toInt()
        val imminentDistanceMeters = thresholdConfig.imminentDistanceMeters * biasMultiplier
        val triggeredByEta = rule.usesTimeTrigger() &&
            etaMinutes != null &&
            etaMinutes <= imminentEtaMinutes
        val triggeredByDistance = rule.usesDistanceTrigger() &&
            distanceMeters != null &&
            distanceMeters <= imminentDistanceMeters
        return triggeredByEta || triggeredByDistance
    }

    private fun stageBiasMultiplier(
        confidence: Confidence,
        alertMode: AlertMode,
    ): Double = when (confidence) {
        Confidence.HIGH -> if (alertMode == AlertMode.SLEEP) {
            thresholdConfig.biasMultiplierHighSleep
        } else {
            thresholdConfig.biasMultiplierHighActive
        }

        Confidence.DEGRADED -> if (alertMode == AlertMode.SLEEP) {
            thresholdConfig.biasMultiplierDegradedSleep
        } else {
            thresholdConfig.biasMultiplierDegradedActive
        }

        Confidence.OFFLINE -> thresholdConfig.biasMultiplierOffline
    }
}
