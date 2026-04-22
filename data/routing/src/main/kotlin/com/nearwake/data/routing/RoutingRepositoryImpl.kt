package com.nearwake.data.routing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutingRepositoryImpl @Inject constructor(
    private val routingDataSource: RoutingDataSource,
    private val localRouteCache: LocalRouteCache,
) : RoutingRepository {
    override suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
    ): Result<RouteSnapshot> =
        routingDataSource.fetchRoute(origin, destination).onSuccess { snapshot ->
            localRouteCache.put(snapshot)
        }

    override suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int> =
        routingDataSource.refreshEta(tripId, currentLocation)

    override suspend fun getCachedRoute(tripId: String): RouteSnapshot? =
        localRouteCache.get(tripId)

    override suspend fun clearCachedRoute(tripId: String) {
        localRouteCache.remove(tripId)
    }
}
