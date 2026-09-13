package com.nearwake.application.monitoring

import com.nearwake.ports.monitoring.TripMonitoringGateway
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AcknowledgeTripAlertUseCaseTest {
    @Test
    fun `forwards the acknowledged trip id to monitoring`() = runBlocking {
        val gateway = RecordingGateway()

        AcknowledgeTripAlertUseCase(gateway)("trip-1")

        assertEquals("trip-1", gateway.acknowledgedTripId)
    }

    private class RecordingGateway : TripMonitoringGateway {
        var acknowledgedTripId: String? = null

        override fun startMonitoring(tripId: String) = Unit

        override fun stopMonitoring() = Unit

        override suspend fun acknowledgeAlert(tripId: String) {
            acknowledgedTripId = tripId
        }
    }
}
