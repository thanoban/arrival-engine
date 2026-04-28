package com.nearwake.application.monitoring

import com.nearwake.ports.monitoring.TripMonitoringGateway
import javax.inject.Inject

class StopTripMonitoringUseCase @Inject constructor(
    private val tripMonitoringGateway: TripMonitoringGateway,
) {
    operator fun invoke() {
        tripMonitoringGateway.stopMonitoring()
    }
}
