package com.nearwake.application.trip

import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.ports.monitoring.TripMonitoringGateway
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    }

    private class FakeTripLifecycleStore : TripLifecycleStore {
        var completedTripId: String? = null
        var completedAt: Instant? = null
        val clearedTripIds = mutableListOf<String>()

        override fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot> =
            flowOf(
                PersistedHomeSnapshot(
                    trips = emptyList(),
                    sessions = emptyList(),
                    savedPlaces = emptyList(),
                ),
            )

        override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? = null

        override suspend fun getTrip(tripId: String): PersistedTrip? =
            PersistedTrip(
                id = tripId,
                destinationId = "place-1",
                alertLeadMinutes = 5,
                alertTriggerMode = AlertTriggerMode.TIME,
                alertDistanceMeters = 500,
                alertIntensity = AlertIntensity.STANDARD,
                alertMode = AlertMode.ACTIVE,
                createdAt = Instant.parse("2026-05-05T02:00:00Z"),
                completedAt = null,
            )

        override suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode) = Unit

        override suspend fun completeTrip(tripId: String, completedAt: Instant) {
            completedTripId = tripId
            this.completedAt = completedAt
        }

        override suspend fun clearTripSession(tripId: String) {
            clearedTripIds += tripId
        }

        override suspend fun markPlaceUsed(placeId: String, usedAt: Instant) = Unit

        override suspend fun saveTrip(command: SaveTripCommand) = Unit

        override suspend fun saveTripSession(command: SaveTripSessionCommand) = Unit
    }
}
