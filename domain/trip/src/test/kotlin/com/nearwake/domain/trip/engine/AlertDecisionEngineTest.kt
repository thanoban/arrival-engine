package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.trip.model.AlertReason
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class AlertDecisionEngineTest {
    private val engine = AlertDecisionEngine()
    private val decidedAt = Instant.parse("2026-04-22T12:00:00Z")

    @Test
    fun `destination geofence triggers alert immediately`() {
        val outcome = engine.decide(
            tripId = "trip-1",
            rule = TripRule(alertLeadMinutes = 10),
            confidence = Confidence.HIGH,
            distanceMeters = 900.0,
            destinationGeofenceEntered = true,
            overshot = false,
            decidedAt = decidedAt,
        )

        assertThat(outcome.action).isEqualTo(AlertAction.ALERT)
        assertThat(outcome.decision?.reason).isEqualTo(AlertReason.GEOFENCE_ENTERED)
    }

    @Test
    fun `distance inside destination radius triggers arrival alert`() {
        val outcome = engine.decide(
            tripId = "trip-2",
            rule = TripRule(alertLeadMinutes = 10, destinationRadiusMeters = 320f),
            confidence = Confidence.HIGH,
            distanceMeters = 250.0,
            destinationGeofenceEntered = false,
            overshot = false,
            decidedAt = decidedAt,
        )

        assertThat(outcome.action).isEqualTo(AlertAction.ALERT)
        assertThat(outcome.decision?.reason).isEqualTo(AlertReason.ETA_THRESHOLD)
    }

    @Test
    fun `overshoot enters recovery when arrival was missed`() {
        val outcome = engine.decide(
            tripId = "trip-3",
            rule = TripRule(alertLeadMinutes = 10),
            confidence = Confidence.OFFLINE,
            distanceMeters = 900.0,
            destinationGeofenceEntered = false,
            overshot = true,
            decidedAt = decidedAt,
        )

        assertThat(outcome.action).isEqualTo(AlertAction.RECOVER)
        assertThat(outcome.decision?.reason).isEqualTo(AlertReason.OVERSHOOT)
    }
}
