package com.nearwake.data.routing

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.database.dao.RouteSnapshotDao
import com.nearwake.core.database.entity.RouteSnapshotEntity
import com.nearwake.core.testing.FakeRoutingDataSource
import com.nearwake.core.testing.TripEngineTestFixtures
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RoutingRepositoryImplTest {
    @Test
    fun `cache route for trip stores trip scoped route snapshot`() = runTest {
        val repository = RoutingRepositoryImpl(
            routingDataSource = FakeRoutingDataSource(),
            localRouteCache = LocalRouteCache(FakeRouteSnapshotDao()),
        )
        val originalSnapshot = TripEngineTestFixtures.routeSnapshot(tripId = "provider-route-id")

        val cachedSnapshot = repository.cacheRouteForTrip(
            tripId = "trip-123",
            routeSnapshot = originalSnapshot,
        )

        assertThat(cachedSnapshot.tripId).isEqualTo("trip-123")
        assertThat(repository.getCachedRoute("trip-123")).isEqualTo(cachedSnapshot)
        assertThat(repository.getCachedRoute("provider-route-id")).isNull()
    }

    private class FakeRouteSnapshotDao : RouteSnapshotDao {
        private val snapshots = mutableMapOf<String, RouteSnapshotEntity>()

        override suspend fun getRouteSnapshotByTripId(tripId: String): RouteSnapshotEntity? =
            snapshots[tripId]

        override suspend fun upsertRouteSnapshot(snapshot: RouteSnapshotEntity) {
            snapshots[snapshot.tripId] = snapshot
        }

        override suspend fun deleteRouteSnapshotByTripId(tripId: String) {
            snapshots.remove(tripId)
        }
    }
}
