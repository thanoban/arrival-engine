package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test

class TransferMonitorTest {
    private val monitor = TransferMonitor()

    @Test
    fun `next transfer becomes soon inside the three minute window`() {
        val state = monitor.evaluate(
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

        assertThat(state.nextTransfer?.stopName).isEqualTo("Central Exchange")
        assertThat(state.nextTransfer?.status).isEqualTo(TransferProgressStatus.SOON)
        assertThat(state.nextTransfer?.remainingMinutes).isEqualTo(2)
    }

    @Test
    fun `past transfers are marked completed while destination remains upcoming`() {
        val state = monitor.evaluate(
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

        assertThat(state.checkpoints.first().status).isEqualTo(TransferProgressStatus.COMPLETED)
        assertThat(state.checkpoints.last().type).isEqualTo(TransferCheckpointType.DESTINATION)
        assertThat(state.checkpoints.last().stopName).isEqualTo("Airport")
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
