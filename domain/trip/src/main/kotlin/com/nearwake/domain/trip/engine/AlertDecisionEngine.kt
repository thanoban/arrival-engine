package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.AlertDecision
import com.nearwake.domain.trip.model.AlertReason
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

enum class AlertAction {
    HOLD,
    ALERT,
    RECOVER,
}

data class AlertDecisionOutcome(
    val action: AlertAction,
    val decision: AlertDecision? = null,
)

class AlertDecisionEngine(
    private val clock: Clock = Clock.System,
) {
    fun decide(
        tripId: String,
        rule: TripRule,
        confidence: Confidence,
        distanceMeters: Double?,
        destinationGeofenceEntered: Boolean,
        overshot: Boolean,
        decidedAt: Instant = clock.now(),
    ): AlertDecisionOutcome {
        if (destinationGeofenceEntered) {
            return AlertDecisionOutcome(
                action = AlertAction.ALERT,
                decision = AlertDecision(
                    tripId = tripId,
                    reason = AlertReason.GEOFENCE_ENTERED,
                    confidence = confidence,
                    decidedAt = decidedAt,
                ),
            )
        }

        if (distanceMeters != null && distanceMeters <= rule.destinationRadiusMeters) {
            return AlertDecisionOutcome(
                action = AlertAction.ALERT,
                decision = AlertDecision(
                    tripId = tripId,
                    reason = AlertReason.ETA_THRESHOLD,
                    confidence = confidence,
                    decidedAt = decidedAt,
                ),
            )
        }

        if (overshot) {
            return AlertDecisionOutcome(
                action = AlertAction.RECOVER,
                decision = AlertDecision(
                    tripId = tripId,
                    reason = AlertReason.OVERSHOOT,
                    confidence = confidence,
                    decidedAt = decidedAt,
                ),
            )
        }

        return AlertDecisionOutcome(action = AlertAction.HOLD)
    }
}
