package com.nearwake.domain.trip.usecase

import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession

data class BatterySummary(
    val mode: MonitoringMode,
    val label: String,
    val estimatedDrain: String,
)

class ComputeBatterySummaryUseCase {
    operator fun invoke(session: TripSession): BatterySummary =
        when (session.monitoringMode) {
            MonitoringMode.GEOFENCE_ONLY -> BatterySummary(
                mode = session.monitoringMode,
                label = "Idle monitoring",
                estimatedDrain = "Minimal battery use",
            )

            MonitoringMode.BALANCED -> BatterySummary(
                mode = session.monitoringMode,
                label = "Balanced tracking",
                estimatedDrain = "Low battery use",
            )

            MonitoringMode.PRECISE_BURST -> BatterySummary(
                mode = session.monitoringMode,
                label = "Precise burst",
                estimatedDrain = "Higher battery use for a short burst",
            )
        }
}
