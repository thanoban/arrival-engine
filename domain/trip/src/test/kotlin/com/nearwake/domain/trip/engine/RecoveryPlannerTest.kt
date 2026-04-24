package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class RecoveryPlannerTest {
    private val planner = RecoveryPlanner()

    @Test
    fun `prefers walk back when destination is nearby`() {
        val plan = planner.plan(
            currentLocation = LatLng(lat = 6.9271, lng = 79.8611),
            destinationLocation = LatLng(lat = 6.9270, lng = 79.8611),
            routeSnapshot = routeSnapshot(),
        )

        assertThat(plan.guidanceMode).isEqualTo(RecoveryGuidanceMode.WALK_BACK)
        assertThat(plan.walkBackDistanceMeters).isLessThan(30)
    }

    @Test
    fun `suggests nearest return stop when destination is farther away`() {
        val plan = planner.plan(
            currentLocation = LatLng(lat = 6.9270, lng = 79.8672),
            destinationLocation = LatLng(lat = 6.9270, lng = 79.8611),
            routeSnapshot = routeSnapshot(),
        )

        assertThat(plan.guidanceMode).isEqualTo(RecoveryGuidanceMode.RETURN_STOP)
        assertThat(plan.returnStopName).isEqualTo("End")
        assertThat(plan.returnStopDistanceMeters).isLessThan(50)
    }

    @Test
    fun `falls back to resume monitoring when location is unavailable`() {
        val plan = planner.plan(
            currentLocation = null,
            destinationLocation = LatLng(lat = 6.9270, lng = 79.8611),
            routeSnapshot = routeSnapshot(),
        )

        assertThat(plan.guidanceMode).isEqualTo(RecoveryGuidanceMode.RESUME_MONITORING)
    }

    private fun routeSnapshot(): RouteSnapshot =
        RouteSnapshot(
            tripId = "trip-1",
            stops = listOf(
                Stop(name = "Start", lat = 6.9270, lng = 79.8611, order = 0),
                Stop(name = "Mid", lat = 6.9270, lng = 79.8645, order = 1),
                Stop(name = "End", lat = 6.9270, lng = 79.8670, order = 2),
            ),
            transfers = emptyList(),
            totalDurationMinutes = 18,
            fetchedAt = Instant.parse("2026-04-24T00:00:00Z"),
            isStale = false,
        )
}
