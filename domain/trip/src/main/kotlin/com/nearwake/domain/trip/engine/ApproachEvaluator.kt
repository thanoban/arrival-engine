package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule

data class ApproachDecision(
    val shouldEscalate: Boolean,
    val effectiveThresholdMinutes: Int,
    val triggeredByDistance: Boolean,
    val triggeredByEta: Boolean,
)

class ApproachEvaluator(
    private val approachBufferMinutes: Int = DEFAULT_APPROACH_BUFFER_MINUTES,
) {
    fun evaluate(
        etaMinutes: Int?,
        distanceMeters: Double?,
        tripRule: TripRule,
        confidence: Confidence,
    ): ApproachDecision {
        val effectiveThresholdMinutes = tripRule.effectiveAlertThresholdMinutes(
            approachBufferMinutes = approachBufferMinutes,
            confidence = confidence,
        )
        val triggeredByEta = tripRule.usesTimeTrigger() &&
            etaMinutes != null &&
            effectiveThresholdMinutes > 0 &&
            etaMinutes <= effectiveThresholdMinutes
        val triggeredByDistance = tripRule.usesDistanceTrigger() &&
            distanceMeters != null &&
            distanceMeters <= tripRule.alertDistanceMeters
        return ApproachDecision(
            shouldEscalate = triggeredByEta || triggeredByDistance,
            effectiveThresholdMinutes = effectiveThresholdMinutes,
            triggeredByDistance = triggeredByDistance,
            triggeredByEta = triggeredByEta,
        )
    }

    companion object {
        const val DEFAULT_APPROACH_BUFFER_MINUTES = 2
    }
}
