package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class BoardingValidatorTest {
    private val validator = BoardingValidator()

    @Test
    fun `returns aligned when movement matches first leg heading`() {
        val result = validator.evaluate(
            routeSnapshot = routeSnapshot(),
            previousLocation = LatLng(lat = 6.9270, lng = 79.8611),
            currentLocation = LatLng(lat = 6.9270, lng = 79.8628),
        )

        assertThat(result.alignment).isEqualTo(BoardingAlignment.ALIGNED)
        assertThat(result.headingDeltaDegrees).isLessThan(20.0)
    }

    @Test
    fun `returns wrong direction when movement opposes route heading`() {
        val result = validator.evaluate(
            routeSnapshot = routeSnapshot(),
            previousLocation = LatLng(lat = 6.9270, lng = 79.8628),
            currentLocation = LatLng(lat = 6.9270, lng = 79.8611),
        )

        assertThat(result.alignment).isEqualTo(BoardingAlignment.WRONG_DIRECTION)
        assertThat(result.headingDeltaDegrees).isGreaterThan(150.0)
    }

    @Test
    fun `returns undetermined when movement is too small`() {
        val result = validator.evaluate(
            routeSnapshot = routeSnapshot(),
            previousLocation = LatLng(lat = 6.9270, lng = 79.8611),
            currentLocation = LatLng(lat = 6.9270, lng = 79.8612),
        )

        assertThat(result.alignment).isEqualTo(BoardingAlignment.UNDETERMINED)
    }

    private fun routeSnapshot(): RouteSnapshot =
        RouteSnapshot(
            tripId = "trip-1",
            stops = listOf(
                Stop(name = "Start", lat = 6.9270, lng = 79.8611, order = 0),
                Stop(name = "Next", lat = 6.9270, lng = 79.8645, order = 1),
                Stop(name = "End", lat = 6.9270, lng = 79.8670, order = 2),
            ),
            transfers = emptyList(),
            totalDurationMinutes = 18,
            fetchedAt = Instant.parse("2026-04-24T00:00:00Z"),
            isStale = false,
        )
}
