package com.nearwake.feature.livetrip

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class TransferProgressBuilderTest {
    @Test
    fun `marks the next transfer as soon when it is within three minutes`() {
        val steps = buildTransferProgress(
            routeSnapshot = routeSnapshot(
                totalDurationMinutes = 40,
                transfers = listOf(
                    TransferPoint(
                        stop = Stop(name = "Central Exchange", lat = 0.0, lng = 0.0, order = 1),
                        lineName = "Blue Line",
                        arrivalMinutes = 18,
                    ),
                ),
            ),
            etaMinutes = 24,
            destinationName = "Airport",
        )

        assertThat(steps[0].timingLabel).isEqualTo("in 2 min")
        assertThat(steps[0].status).isEqualTo(TransferProgressStatus.Soon)
    }

    @Test
    fun `marks passed transfers as completed once elapsed time is beyond transfer minute`() {
        val steps = buildTransferProgress(
            routeSnapshot = routeSnapshot(
                totalDurationMinutes = 40,
                transfers = listOf(
                    TransferPoint(
                        stop = Stop(name = "Central Exchange", lat = 0.0, lng = 0.0, order = 1),
                        lineName = "Blue Line",
                        arrivalMinutes = 12,
                    ),
                ),
            ),
            etaMinutes = 20,
            destinationName = "Airport",
        )

        assertThat(steps[0].timingLabel).isEqualTo("Passed")
        assertThat(steps[0].status).isEqualTo(TransferProgressStatus.Completed)
        assertThat(steps.last().title).isEqualTo("Arrive at Airport")
    }

    private fun routeSnapshot(
        totalDurationMinutes: Int,
        transfers: List<TransferPoint>,
    ) = RouteSnapshot(
        tripId = "trip-1",
        stops = listOf(
            Stop(name = "Start", lat = 0.0, lng = 0.0, order = 0),
            Stop(name = "Airport", lat = 1.0, lng = 1.0, order = 2),
        ),
        transfers = transfers,
        totalDurationMinutes = totalDurationMinutes,
        fetchedAt = Instant.parse("2026-04-24T08:00:00Z"),
        isStale = false,
    )
}
