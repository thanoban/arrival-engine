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
import org.junit.jupiter.api.Test

class ObserveTripSummaryUseCaseTest {
    @Test
    fun `builds trip summary from persisted snapshot and cached route`() = runBlocking {
        val useCase = ObserveTripSummaryUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "trip-1",
                            destinationId = "place-1",
                            alertLeadMinutes = 10,
                            alertTriggerMode = AlertTriggerMode.TIME,
                            alertDistanceMeters = 400,
                            alertIntensity = AlertIntensity.STANDARD,
                            alertMode = AlertMode.ACTIVE,
                            createdAt = Instant.parse("2026-05-04T05:30:00Z"),
                            completedAt = null,
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.Alerting,
                            monitoringMode = MonitoringMode.PRECISE_BURST,
                            alertStage = AlertStage.IMMINENT,
                            lastEtaMinutes = 3,
                            confidence = Confidence.OFFLINE,
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace("place-1", "Colombo Fort", "Fort Station", 6.93, 79.84),
                    ),
                ),
            ),
            routingRepository = FakeRoutingRepository(),
        )

        val summary = useCase("trip-1").first()

        assertEquals("Monitoring in progress", summary.statusLabel)
        assertEquals("Precise burst", summary.monitoringLabel)
        assertEquals("1 stop · 0 transfers", summary.routeSummary)
        assertEquals("Low confidence", summary.confidenceLabel)
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
                stops = listOf(
                    Stop(
                        name = "Fort Station",
                        lat = 6.93,
                        lng = 79.84,
                        order = 0,
                    ),
                ),
                transfers = emptyList(),
                totalDurationMinutes = 15,
                fetchedAt = Instant.parse("2026-05-04T05:32:00Z"),
                isStale = false,
            )

        override suspend fun clearCachedRoute(tripId: String) = Unit
    }
}
