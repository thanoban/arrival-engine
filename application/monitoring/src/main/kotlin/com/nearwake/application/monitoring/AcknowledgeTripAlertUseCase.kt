package com.nearwake.application.monitoring

import com.nearwake.ports.monitoring.TripMonitoringGateway
import javax.inject.Inject

class AcknowledgeTripAlertUseCase @Inject constructor(
    private val tripMonitoringGateway: TripMonitoringGateway,
) {
    suspend operator fun invoke(tripId: String) {
        tripMonitoringGateway.acknowledgeAlert(tripId)
    }
}
