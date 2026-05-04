package com.nearwake.application.trip

import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject

class CancelTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
) {
    suspend operator fun invoke(tripId: String) {
        tripLifecycleStore.clearTripSession(tripId)
        stopTripMonitoring()
    }
}
