package com.nearwake.core.testing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.model.RouteSnapshot

class FakeRoutingDataSource(
    var fetchRouteResult: Result<RouteSnapshot> = Result.success(TripEngineTestFixtures.routeSnapshot()),
    var refreshEtaResult: Result<Int> = Result.success(12),
) : RoutingDataSource {
    val fetchRouteRequests = mutableListOf<FetchRouteRequest>()
    val refreshEtaRequests = mutableListOf<RefreshEtaRequest>()

    override suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
    ): Result<RouteSnapshot> {
        fetchRouteRequests += FetchRouteRequest(origin = origin, destination = destination)
        return fetchRouteResult
    }

    override suspend fun refreshEta(
        tripId: String,
        currentLocation: LatLng,
    ): Result<Int> {
        refreshEtaRequests += RefreshEtaRequest(tripId = tripId, currentLocation = currentLocation)
        return refreshEtaResult
    }

    data class FetchRouteRequest(
        val origin: LatLng,
        val destination: LatLng,
    )

    data class RefreshEtaRequest(
        val tripId: String,
        val currentLocation: LatLng,
    )
}
