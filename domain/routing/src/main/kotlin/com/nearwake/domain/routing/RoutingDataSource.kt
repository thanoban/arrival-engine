package com.nearwake.domain.routing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot

interface RoutingDataSource {
    suspend fun fetchRoute(origin: LatLng, destination: LatLng): Result<RouteSnapshot>

    suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int>
}
