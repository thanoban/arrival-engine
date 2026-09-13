package com.nearwake.feature.livetrip

import com.nearwake.application.trip.LiveTripTransferStatus
import com.nearwake.application.trip.LiveTripTransferStep

internal fun buildTransferProgress(
    steps: List<LiveTripTransferStep>,
): List<TransferProgressUiState> = steps.map { step ->
    TransferProgressUiState(
        title = step.title,
        subtitle = step.subtitle,
        timingLabel = step.timingLabel,
        status = when (step.status) {
            LiveTripTransferStatus.Completed -> TransferProgressStatus.Completed
            LiveTripTransferStatus.Soon -> TransferProgressStatus.Soon
            LiveTripTransferStatus.Upcoming -> TransferProgressStatus.Upcoming
            LiveTripTransferStatus.Final -> TransferProgressStatus.Final
        },
        signalQuality = step.signalQuality,
    )
}
