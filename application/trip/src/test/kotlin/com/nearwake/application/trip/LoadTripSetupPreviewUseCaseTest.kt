package com.nearwake.application.trip

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.ports.persistence.PersistedHomeSnapshot
import com.nearwake.ports.persistence.PersistedSavedPlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoadTripSetupPreviewUseCaseTest {
    @Test
    fun `returns destination unavailable when place is missing`() = runBlocking {
        val useCase = LoadTripSetupPreviewUseCase(
            tripLifecycleStore = TestTripLifecycleStore(
                PersistedHomeSnapshot(
                    trips = emptyList(),
                    sessions = emptyList(),
                    savedPlaces = emptyList(),
                ),
            ),
            locationRepository = FakeLocationRepository(null),
            routingRepository = FakeRoutingRepository(Result.failure(IllegalStateException("unused"))),
            analytics = FakeNearWakeAnalytics(),
        )

        val preview = useCase("missing-place")

        assertFalse(preview.canStart)
        assertEquals("Destination unavailable", preview.destinationName)
        assertNull(preview.previewRouteSnapshot)
    }

    @Test
    fun `falls back to destination only when origin is unavailable`() = runBlocking {
        val useCase = LoadTripSetupPreviewUseCase(
            tripLifecycleStore = storeWithPlace(),
            locationRepository = FakeLocationRepository(null),
            routingRepository = FakeRoutingRepository(Result.failure(IllegalStateException("unused"))),
            analytics = FakeNearWakeAnalytics(),
        )

        val preview = useCase("place-1")

        assertTrue(preview.canStart)
        assertEquals("Destination-only", preview.etaLabel)
        assertNull(preview.previewRouteSnapshot)
    }

    @Test
    fun `returns route preview when routing succeeds`() = runBlocking {
        val routeSnapshot = RouteSnapshot(
            tripId = "preview",
            stops = listOf(
                Stop(name = "Maharagama", lat = 6.8480, lng = 79.9265, order = 0),
                Stop(name = "Colombo Fort", lat = 6.9344, lng = 79.8428, order = 1),
            ),
            transfers = emptyList(),
            totalDurationMinutes = 32,
            fetchedAt = Instant.parse("2026-05-06T02:00:00Z"),
            isStale = false,
        )
        val useCase = LoadTripSetupPreviewUseCase(
            tripLifecycleStore = storeWithPlace(),
            locationRepository = FakeLocationRepository(LatLng(lat = 6.9000, lng = 79.8500)),
            routingRepository = FakeRoutingRepository(Result.success(routeSnapshot)),
            analytics = FakeNearWakeAnalytics(),
        )

        val preview = useCase("place-1")

        assertTrue(preview.canStart)
        assertEquals("~32 min", preview.etaLabel)
        assertEquals("2 stops · 0 transfers", preview.routeSummary)
        assertEquals(routeSnapshot, preview.previewRouteSnapshot)
    }

    private fun storeWithPlace() = TestTripLifecycleStore(
        PersistedHomeSnapshot(
            trips = emptyList(),
            sessions = emptyList(),
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
    )

    private class FakeLocationRepository(
        private val lastKnownLocation: LatLng?,
    ) : LocationRepository {
        override suspend fun getLastKnownLocation(): LatLng? = lastKnownLocation

        override fun observeBalancedUpdates(): Flow<LatLng> = emptyFlow()

        override fun observePreciseUpdates(): Flow<LatLng> = emptyFlow()

        override suspend fun stopLocationUpdates() = Unit
    }

    private class FakeRoutingRepository(
        private val routeResult: Result<RouteSnapshot>,
    ) : RoutingRepository {
        override suspend fun fetchRoute(
            origin: LatLng,
            destination: LatLng,
        ): Result<RouteSnapshot> = routeResult

        override suspend fun cacheRouteForTrip(tripId: String, routeSnapshot: RouteSnapshot): RouteSnapshot = routeSnapshot

        override suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int> =
            error("unused")

        override suspend fun getCachedRoute(tripId: String): RouteSnapshot? = null

        override suspend fun clearCachedRoute(tripId: String) = Unit
    }
}
