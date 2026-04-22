package com.nearwake.data.routing

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import kotlinx.datetime.Clock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoutingStubsTest {
    @Test
    fun `no op routing reports destination only mode`() = runTest {
        val result = NoOpRoutingDataSource().fetchRoute(
            origin = LatLng(6.9271, 79.8612),
            destination = LatLng(7.1808, 79.8841),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("destination-only mode")
    }

    @Test
    fun `google transit data source clearly reports phase two status`() = runTest {
        val result = GoogleTransitDataSource().refreshEta(
            tripId = "trip-123",
            currentLocation = LatLng(6.9271, 79.8612),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("Phase 2")
    }

    @Test
    fun `local route cache stores updates and removals`() = runTest {
        val cache = LocalRouteCache()
        val snapshot = RouteSnapshot(
            tripId = "trip-123",
            stops = listOf(
                Stop(name = "Central Station", lat = 6.9271, lng = 79.8612, order = 1),
                Stop(name = "Airport Terminal 2", lat = 7.1808, lng = 79.8841, order = 2),
            ),
            transfers = listOf(
                TransferPoint(
                    stop = Stop(name = "Central Station", lat = 6.9271, lng = 79.8612, order = 1),
                    lineName = "Blue Line",
                    arrivalMinutes = 12,
                ),
            ),
            totalDurationMinutes = 42,
            fetchedAt = Clock.System.now(),
            isStale = false,
        )

        assertThat(cache.get(snapshot.tripId)).isNull()

        cache.put(snapshot)
        assertThat(cache.get(snapshot.tripId)).isEqualTo(snapshot)

        cache.remove(snapshot.tripId)
        assertThat(cache.get(snapshot.tripId)).isNull()
    }
}
