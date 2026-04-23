package com.nearwake.domain.routing.repository

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot

interface RoutingRepository {
    suspend fun fetchRoute(origin: LatLng, destination: LatLng): Result<RouteSnapshot>

    suspend fun cacheRouteForTrip(tripId: String, routeSnapshot: RouteSnapshot): RouteSnapshot

    suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int>

    suspend fun getCachedRoute(tripId: String): RouteSnapshot?

    suspend fun clearCachedRoute(tripId: String)
}
