package com.nearwake.data.routing

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.RoutingDataSource
import com.nearwake.domain.routing.model.RouteSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleTransitDataSource @Inject constructor() : RoutingDataSource {
    override suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
    ): Result<RouteSnapshot> {
        // TODO Phase 2: call Google Directions API with transit mode and map the response.
        return Result.failure(
            NotImplementedError("Google transit routing is planned for Phase 2."),
        )
    }

    override suspend fun refreshEta(
        tripId: String,
        currentLocation: LatLng,
    ): Result<Int> {
        // TODO Phase 2: refresh ETA from the route provider using the current location.
        return Result.failure(
            NotImplementedError("Google transit ETA refresh is planned for Phase 2."),
        )
    }
}
