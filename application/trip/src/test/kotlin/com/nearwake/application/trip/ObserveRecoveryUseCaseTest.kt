package com.nearwake.application.trip

import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObserveRecoveryUseCaseTest {
    @Test
    fun `builds walk-back guidance from persisted location context`() = runBlocking {
        val useCase = ObserveRecoveryUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
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
                            createdAt = Instant.parse("2026-05-05T01:00:00Z"),
                            completedAt = null,
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.Recovery,
                            monitoringMode = MonitoringMode.BALANCED,
                            alertStage = AlertStage.RECOVERY,
                            lastKnownLat = 6.9345,
                            lastKnownLng = 79.8429,
                            lastEtaMinutes = 4,
                            confidence = Confidence.DEGRADED,
                            updatedAt = Instant.parse("2026-05-05T01:20:00Z"),
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace("place-1", "Colombo Fort", "Fort Station", 6.9344, 79.8428),
                    ),
                ),
            ),
            routingRepository = FakeRoutingRepository(),
        )

        val recovery = useCase("trip-1").first()

        assertEquals("Colombo Fort", recovery.destinationName)
        assertEquals("Medium confidence", recovery.confidenceLabel)
        assertTrue(recovery.recoveryGuidanceLabel.contains("recover on foot"))
    }

    private class FakeRoutingRepository : RoutingRepository {
        override suspend fun fetchRoute(
            origin: com.nearwake.domain.location.model.LatLng,
            destination: com.nearwake.domain.location.model.LatLng,
        ): Result<RouteSnapshot> = error("unused")

        override suspend fun cacheRouteForTrip(tripId: String, routeSnapshot: RouteSnapshot): RouteSnapshot = routeSnapshot

        override suspend fun refreshEta(
            tripId: String,
            currentLocation: com.nearwake.domain.location.model.LatLng,
        ): Result<Int> = error("unused")

        override suspend fun getCachedRoute(tripId: String): RouteSnapshot =
            RouteSnapshot(
                tripId = tripId,
                stops = listOf(Stop(name = "Fort Station", lat = 6.9344, lng = 79.8428, order = 0)),
                transfers = emptyList(),
                totalDurationMinutes = 15,
                fetchedAt = Instant.parse("2026-05-05T01:10:00Z"),
                isStale = false,
            )

        override suspend fun clearCachedRoute(tripId: String) = Unit
    }
}
