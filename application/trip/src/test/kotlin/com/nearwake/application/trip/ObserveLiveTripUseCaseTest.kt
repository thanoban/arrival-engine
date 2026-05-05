package com.nearwake.application.trip

import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.RouteSignalQuality
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
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

class ObserveLiveTripUseCaseTest {
    @Test
    fun `builds live trip presentation from persisted session and cached route`() = runBlocking {
        val useCase = ObserveLiveTripUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = listOf(
                        PersistedTrip(
                            id = "trip-1",
                            destinationId = "place-1",
                            alertLeadMinutes = 10,
                            alertTriggerMode = AlertTriggerMode.TIME,
                            alertDistanceMeters = 600,
                            alertIntensity = AlertIntensity.STANDARD,
                            alertMode = AlertMode.SLEEP,
                            createdAt = Instant.parse("2026-05-05T02:00:00Z"),
                            completedAt = null,
                        ),
                    ),
                    sessions = listOf(
                        PersistedTripSession(
                            tripId = "trip-1",
                            state = TripState.MonitoringApproach,
                            monitoringMode = MonitoringMode.BALANCED,
                            alertStage = AlertStage.APPROACH,
                            lastKnownLat = 6.9340,
                            lastKnownLng = 79.8420,
                            lastEtaMinutes = 7,
                            confidence = Confidence.DEGRADED,
                            updatedAt = Instant.parse("2026-05-05T02:05:00Z"),
                        ),
                    ),
                    savedPlaces = listOf(
                        PersistedSavedPlace("place-1", "Colombo Fort", "Fort Station", 6.9344, 79.8428),
                    ),
                ),
            ),
            routingRepository = FakeRoutingRepository(),
        )

        val liveTrip = useCase("trip-1").first()

        assertEquals("Colombo Fort", liveTrip.destinationName)
        assertEquals("~7 min", liveTrip.etaLabel)
        assertEquals(MonitoringMode.BALANCED, liveTrip.monitoringMode)
        assertEquals(Confidence.DEGRADED, liveTrip.confidence)
        assertTrue(liveTrip.alertSummary.contains("10 min early"))
        assertEquals(2, liveTrip.transferSteps.size)
        assertEquals(LiveTripTransferStatus.Completed, liveTrip.transferSteps.first().status)
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
                    Stop(name = "Boarding stop", lat = 6.9300, lng = 79.8400, order = 0),
                    Stop(name = "Transfer stop", lat = 6.9320, lng = 79.8410, order = 1, signalQuality = RouteSignalQuality.DEGRADED),
                    Stop(name = "Fort Station", lat = 6.9344, lng = 79.8428, order = 2),
                ),
                transfers = listOf(
                    TransferPoint(
                        stop = Stop(
                            name = "Transfer stop",
                            lat = 6.9320,
                            lng = 79.8410,
                            order = 1,
                            signalQuality = RouteSignalQuality.DEGRADED,
                        ),
                        lineName = "Coastal Line",
                        arrivalMinutes = 1,
                    ),
                ),
                totalDurationMinutes = 18,
                fetchedAt = Instant.parse("2026-05-05T02:01:00Z"),
                isStale = false,
            )

        override suspend fun clearCachedRoute(tripId: String) = Unit
    }
}
