package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ObserveHomeDashboardUseCaseTest {
    @Test
    fun `builds monitoring dashboard from persisted snapshot`() = runBlocking {
        val useCase = ObserveHomeDashboardUseCase(
            tripLifecycleStore = FakeTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "trip-1",
                            destinationId = "place-1",
                            alertLeadMinutes = 5,
                            alertTriggerMode = AlertTriggerMode.TIME,
                            alertDistanceMeters = 500,
                            alertIntensity = AlertIntensity.STANDARD,
                            alertMode = AlertMode.ACTIVE,
                            createdAt = Instant.parse("2026-05-04T05:30:00Z"),
                            completedAt = null,
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.MonitoringApproach,
                            alertStage = AlertStage.APPROACH,
                            lastEtaMinutes = 8,
                            confidence = Confidence.DEGRADED,
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace(
                            id = "place-1",
                            name = "Colombo Fort",
                            address = "Fort Station",
                            lat = 6.9344,
                            lng = 79.8428,
                        ),
                    ),
                ),
            ),
        )

        val dashboard = useCase().first()

        assertEquals("Monitoring now", dashboard.statusLabel)
        assertEquals(HomeDashboardTone.Monitoring, dashboard.statusTone)
        assertEquals("NearWake is already guarding your current ride.", dashboard.headline)
        assertNotNull(dashboard.activeTrip)
        assertEquals("Colombo Fort", dashboard.activeTrip?.destinationName)
        assertEquals("Approach window · ~8 min · Medium confidence", dashboard.activeTrip?.subtitle)
    }

    private class FakeTripLifecycleStore(
        private val snapshot: PersistedHomeSnapshot,
    ) : TripLifecycleStore {
        override fun observeHomeSnapshot(): Flow<PersistedHomeSnapshot> = flowOf(snapshot)

        override suspend fun getSavedPlace(placeId: String): PersistedSavedPlace? = null

        override suspend fun getTrip(tripId: String): PersistedTrip? = null

        override suspend fun updateTripAlertMode(tripId: String, alertMode: AlertMode) = Unit

        override suspend fun clearTripSession(tripId: String) = Unit

        override suspend fun markPlaceUsed(placeId: String, usedAt: Instant) = Unit

        override suspend fun saveTrip(command: SaveTripCommand) = Unit

        override suspend fun saveTripSession(command: SaveTripSessionCommand) = Unit
    }
}
