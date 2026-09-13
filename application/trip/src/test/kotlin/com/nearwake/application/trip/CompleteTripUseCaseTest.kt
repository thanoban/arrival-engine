package com.nearwake.application.trip

import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.ports.monitoring.TripMonitoringGateway
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompleteTripUseCaseTest {
    @Test
    fun `completes trip clears session and stops monitoring`() = runBlocking {
        val tripStore = FakeTripLifecycleStore()
        val monitoringGateway = FakeTripMonitoringGateway()
        val useCase = CompleteTripUseCase(
            tripLifecycleStore = tripStore,
            stopTripMonitoring = StopTripMonitoringUseCase(monitoringGateway),
            analytics = FakeNearWakeAnalytics(),
        )
        val completedAt = Instant.parse("2026-05-05T03:00:00Z")

        useCase(tripId = "trip-1", completedAt = completedAt)

        assertEquals(completedAt, tripStore.completedAt)
        assertEquals("trip-1", tripStore.completedTripId)
        assertTrue(tripStore.clearedTripIds.contains("trip-1"))
        assertEquals(1, monitoringGateway.stopCalls)
    }

    private class FakeTripMonitoringGateway : TripMonitoringGateway {
        var stopCalls = 0

        override fun startMonitoring(tripId: String) = Unit

        override fun stopMonitoring() {
            stopCalls += 1
        }

        override suspend fun acknowledgeAlert(tripId: String) = Unit
    }
}
