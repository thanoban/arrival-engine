package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripRule
import org.junit.jupiter.api.Test

class ApproachEvaluatorTest {
    private val evaluator = ApproachEvaluator()

    @Test
    fun `offline bias expands the threshold`() {
        val rule = TripRule(alertLeadMinutes = 10)

        val online = evaluator.evaluate(
            etaMinutes = 14,
            distanceMeters = null,
            tripRule = rule,
            confidence = Confidence.HIGH,
        )
        val offline = evaluator.evaluate(
            etaMinutes = 14,
            distanceMeters = null,
            tripRule = rule,
            confidence = Confidence.OFFLINE,
        )

        assertThat(online.shouldEscalate).isFalse()
        assertThat(offline.shouldEscalate).isTrue()
        assertThat(offline.effectiveThresholdMinutes).isGreaterThan(online.effectiveThresholdMinutes)
    }

    @Test
    fun `distance trigger ignores eta and escalates from distance`() {
        val rule = TripRule(
            alertLeadMinutes = 10,
            alertTriggerMode = AlertTriggerMode.DISTANCE,
            alertDistanceMeters = 500,
        )

        val decision = evaluator.evaluate(
            etaMinutes = 3,
            distanceMeters = 450.0,
            tripRule = rule,
            confidence = Confidence.HIGH,
        )

        assertThat(decision.triggeredByEta).isFalse()
        assertThat(decision.triggeredByDistance).isTrue()
        assertThat(decision.shouldEscalate).isTrue()
    }
}
