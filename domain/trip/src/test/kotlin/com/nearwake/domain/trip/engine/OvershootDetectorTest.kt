package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.trip.model.TripRule
import org.junit.jupiter.api.Test

class OvershootDetectorTest {
    private val detector = OvershootDetector()
    private val tripRule = TripRule(alertLeadMinutes = 10, destinationRadiusMeters = 300f)

    @Test
    fun `reports overshoot when user was near stop and then moved away`() {
        val overshot = detector.hasOvershot(
            previousDistanceMeters = 280.0,
            currentDistanceMeters = 520.0,
            tripRule = tripRule,
        )

        assertThat(overshot).isTrue()
    }

    @Test
    fun `does not report overshoot without previous distance`() {
        val overshot = detector.hasOvershot(
            previousDistanceMeters = null,
            currentDistanceMeters = 520.0,
            tripRule = tripRule,
        )

        assertThat(overshot).isFalse()
    }

    @Test
    fun `does not report overshoot when user is still moving toward stop`() {
        val overshot = detector.hasOvershot(
            previousDistanceMeters = 620.0,
            currentDistanceMeters = 410.0,
            tripRule = tripRule,
        )

        assertThat(overshot).isFalse()
    }
}
