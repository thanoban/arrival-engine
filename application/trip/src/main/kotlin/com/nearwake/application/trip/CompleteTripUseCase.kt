package com.nearwake.application.trip

import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CompleteTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
) {
    suspend operator fun invoke(
        tripId: String,
        completedAt: Instant = Clock.System.now(),
    ) {
        tripLifecycleStore.completeTrip(tripId = tripId, completedAt = completedAt)
        tripLifecycleStore.clearTripSession(tripId)
        stopTripMonitoring()
    }
}
