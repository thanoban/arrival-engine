package com.nearwake.application.monitoring

import com.nearwake.ports.monitoring.TripMonitoringGateway
import javax.inject.Inject

class StartTripMonitoringUseCase @Inject constructor(
    private val tripMonitoringGateway: TripMonitoringGateway,
) {
    operator fun invoke(tripId: String) {
        tripMonitoringGateway.startMonitoring(tripId)
    }
}
