package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripState
import org.junit.jupiter.api.Test

class AlertStageEvaluatorTest {
    private val evaluator = AlertStageEvaluator()
    private val rule = TripRule(alertLeadMinutes = 10)

    @Test
    fun `approach stage begins when eta enters the stage a window`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.MONITORING,
            tripState = TripState.MonitoringLowPower,
            rule = rule,
            alertMode = AlertMode.ACTIVE,
            confidence = Confidence.HIGH,
            etaMinutes = 5,
            distanceMeters = 900.0,
            destinationGeofenceEntered = false,
        )

        assertThat(stage).isEqualTo(AlertStage.APPROACH)
    }

    @Test
    fun `imminent stage biases earlier when confidence is degraded`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.APPROACH,
            tripState = TripState.MonitoringApproach,
            rule = rule,
            alertMode = AlertMode.ACTIVE,
            confidence = Confidence.DEGRADED,
            etaMinutes = 3,
            distanceMeters = 340.0,
            destinationGeofenceEntered = false,
        )

        assertThat(stage).isEqualTo(AlertStage.IMMINENT)
    }

    @Test
    fun `distance trigger ignores eta and enters approach from configured radius`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.MONITORING,
            tripState = TripState.MonitoringLowPower,
            rule = TripRule(
                alertLeadMinutes = 10,
                alertTriggerMode = AlertTriggerMode.DISTANCE,
                alertDistanceMeters = 500,
            ),
            alertMode = AlertMode.ACTIVE,
            confidence = Confidence.HIGH,
            etaMinutes = 2,
            distanceMeters = 480.0,
            destinationGeofenceEntered = false,
        )

        assertThat(stage).isEqualTo(AlertStage.APPROACH)
    }

    @Test
    fun `time trigger reaches approach at selected lead minutes`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.MONITORING,
            tripState = TripState.MonitoringLowPower,
            rule = TripRule(
                alertLeadMinutes = 10,
                alertTriggerMode = AlertTriggerMode.TIME,
            ),
            alertMode = AlertMode.ACTIVE,
            confidence = Confidence.HIGH,
            etaMinutes = 10,
            distanceMeters = 900.0,
            destinationGeofenceEntered = false,
        )

        assertThat(stage).isEqualTo(AlertStage.APPROACH)
    }

    @Test
    fun `arrival wins when destination geofence is entered`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.IMMINENT,
            tripState = TripState.MonitoringApproach,
            rule = rule,
            alertMode = AlertMode.SLEEP,
            confidence = Confidence.HIGH,
            etaMinutes = 1,
            distanceMeters = 40.0,
            destinationGeofenceEntered = true,
        )

        assertThat(stage).isEqualTo(AlertStage.ARRIVAL)
    }

    @Test
    fun `stages do not regress after imminent`() {
        val stage = evaluator.evaluate(
            currentStage = AlertStage.IMMINENT,
            tripState = TripState.MonitoringApproach,
            rule = rule,
            alertMode = AlertMode.ACTIVE,
            confidence = Confidence.HIGH,
            etaMinutes = 8,
            distanceMeters = 700.0,
            destinationGeofenceEntered = false,
        )

        assertThat(stage).isEqualTo(AlertStage.IMMINENT)
    }
}
