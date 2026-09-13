package com.nearwake.application.trip

import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.ports.monitoring.TripMonitoringGateway
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TripCreationEtaTest {
    @Test
    fun `starting without a route persists no eta instead of a fabricated value`() = runBlocking {
        val store = TestTripLifecycleStore(snapshot(place = place()))
        val useCase = StartTripUseCase(
            tripLifecycleStore = store,
            routingRepository = NoRouteRepository(),
            startTripMonitoring = StartTripMonitoringUseCase(NoOpMonitoringGateway()),
            analytics = FakeNearWakeAnalytics(),
        )

        val tripId = useCase(
            StartTripRequest(
                placeId = "place-1",
                alertLeadMinutes = 5,
                alertTriggerMode = AlertTriggerMode.TIME,
                alertDistanceMeters = 500,
                alertIntensity = AlertIntensity.STANDARD,
                alertMode = AlertMode.ACTIVE,
            ),
        )

        assertNotNull(tripId)
        assertNull(store.savedTripSession?.lastEtaMinutes)
    }

    @Test
    fun `rearming without a route persists no eta instead of a fabricated value`() = runBlocking {
        val place = place()
        val sourceTrip = PersistedTrip(
            id = "source-trip",
            destinationId = place.id,
            alertLeadMinutes = 5,
            alertTriggerMode = AlertTriggerMode.TIME,
            alertDistanceMeters = 500,
            alertIntensity = AlertIntensity.STANDARD,
            alertMode = AlertMode.ACTIVE,
            createdAt = Instant.parse("2026-09-13T00:00:00Z"),
            completedAt = Instant.parse("2026-09-13T01:00:00Z"),
        )
        val store = TestTripLifecycleStore(snapshot(place = place, trip = sourceTrip))
        val useCase = RearmTripUseCase(
            tripLifecycleStore = store,
            locationRepository = NoLocationRepository(),
            routingRepository = NoRouteRepository(),
            startTripMonitoring = StartTripMonitoringUseCase(NoOpMonitoringGateway()),
            analytics = FakeNearWakeAnalytics(),
        )

        val tripId = useCase("source-trip")

        assertNotNull(tripId)
        assertNull(store.savedTripSession?.lastEtaMinutes)
    }

    private fun place() = PersistedSavedPlace(
        id = "place-1",
        name = "Fort",
        address = "Colombo",
        lat = 6.9344,
        lng = 79.8428,
    )

    private fun snapshot(
        place: PersistedSavedPlace,
        trip: PersistedTrip? = null,
    ) = PersistedHomeSnapshot(
        trips = listOfNotNull(trip),
        sessions = emptyList(),
        savedPlaces = listOf(place),
    )

    private class NoOpMonitoringGateway : TripMonitoringGateway {
        override fun startMonitoring(tripId: String) = Unit

        override fun stopMonitoring() = Unit
    }

    private class NoLocationRepository : LocationRepository {
        override suspend fun getLastKnownLocation(): LatLng? = null

        override fun observeBalancedUpdates(): Flow<LatLng> = emptyFlow()

        override fun observePreciseUpdates(): Flow<LatLng> = emptyFlow()

        override suspend fun stopLocationUpdates() = Unit
    }

    private class NoRouteRepository : RoutingRepository {
        override suspend fun fetchRoute(origin: LatLng, destination: LatLng): Result<RouteSnapshot> =
            Result.failure(IllegalStateException("Route unavailable"))

        override suspend fun cacheRouteForTrip(tripId: String, routeSnapshot: RouteSnapshot): RouteSnapshot =
            routeSnapshot

        override suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int> =
            Result.failure(IllegalStateException("ETA unavailable"))

        override suspend fun getCachedRoute(tripId: String): RouteSnapshot? = null

        override suspend fun clearCachedRoute(tripId: String) = Unit
    }
}
