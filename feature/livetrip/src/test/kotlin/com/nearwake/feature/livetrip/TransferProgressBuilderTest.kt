package com.nearwake.feature.livetrip

import com.google.common.truth.Truth.assertThat
import com.nearwake.application.trip.LiveTripTransferStatus
import com.nearwake.application.trip.LiveTripTransferStep
import com.nearwake.domain.routing.model.RouteSignalQuality
import org.junit.jupiter.api.Test

class TransferProgressBuilderTest {
    @Test
    fun `maps application transfer status and signal quality to UI state`() {
        val steps = buildTransferProgress(
            listOf(
                LiveTripTransferStep(
                    title = "Change to Blue Line",
                    subtitle = "Central Exchange",
                    timingLabel = "in 2 min",
                    status = LiveTripTransferStatus.Soon,
                    signalQuality = RouteSignalQuality.DEGRADED,
                ),
            ),
        )

        assertThat(steps.single().status).isEqualTo(TransferProgressStatus.Soon)
        assertThat(steps.single().signalQuality).isEqualTo(RouteSignalQuality.DEGRADED)
    }

    @Test
    fun `maps every supported transfer status`() {
        val steps = buildTransferProgress(
            LiveTripTransferStatus.entries.map { status ->
                LiveTripTransferStep(
                    title = status.name,
                    subtitle = "Stop",
                    timingLabel = "Now",
                    status = status,
                )
            },
        )

        assertThat(steps.map { it.status }).containsExactly(
            TransferProgressStatus.Completed,
            TransferProgressStatus.Soon,
            TransferProgressStatus.Upcoming,
            TransferProgressStatus.Final,
        ).inOrder()
    }

    @Test
    fun `empty application progress remains empty`() {
        assertThat(
            buildTransferProgress(
                emptyList(),
            ),
        ).isEmpty()
    }
}
