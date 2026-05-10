package com.nearwake.application.trip

import com.nearwake.ports.analytics.NearWakeAnalytics
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject

data class TripSetupPreview(
    val destinationName: String,
    val destinationAddress: String,
    val etaLabel: String,
    val routeSummary: String,
    val canStart: Boolean,
    val previewRouteSnapshot: RouteSnapshot? = null,
)

class LoadTripSetupPreviewUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val locationRepository: LocationRepository,
    private val routingRepository: RoutingRepository,
    private val analytics: NearWakeAnalytics,
) {
    suspend operator fun invoke(placeId: String): TripSetupPreview {
        val place = tripLifecycleStore.getSavedPlace(placeId)
        if (place == null) {
            return TripSetupPreview(
                destinationName = "Destination unavailable",
                destinationAddress = "",
                etaLabel = "Destination unavailable",
                routeSummary = "Pick another destination to start a trip.",
                canStart = false,
            )
        }

        val destination = LatLng(lat = place.lat, lng = place.lng)
        val origin = runCatching { locationRepository.getLastKnownLocation() }
            .onFailure { error ->
                analytics.recordFailure(
                    surface = "trip_setup_last_known_location",
                    throwable = error,
                    attributes = mapOf("place_id" to placeId),
                )
            }
            .getOrNull()
        if (origin == null) {
            return TripSetupPreview(
                destinationName = place.name,
                destinationAddress = place.address,
                etaLabel = "Destination-only",
                routeSummary = "No last known location is available yet, so NearWake will arm destination-only monitoring.",
                canStart = true,
            )
        }

        val routeResult = routingRepository.fetchRoute(origin = origin, destination = destination)
        return routeResult.fold(
            onSuccess = { routeSnapshot ->
                TripSetupPreview(
                    destinationName = place.name,
                    destinationAddress = place.address,
                    etaLabel = "~${routeSnapshot.totalDurationMinutes} min",
                    routeSummary = buildRouteSummary(routeSnapshot),
                    canStart = true,
                    previewRouteSnapshot = routeSnapshot,
                )
            },
            onFailure = { error ->
                analytics.recordFailure(
                    surface = "trip_setup_route_preview",
                    throwable = error,
                    attributes = mapOf("place_id" to placeId),
                )
                TripSetupPreview(
                    destinationName = place.name,
                    destinationAddress = place.address,
                    etaLabel = "Destination-only",
                    routeSummary = "Transit routing is unavailable on this device right now, so NearWake will monitor only the destination.",
                    canStart = true,
                )
            },
        )
    }

    private fun buildRouteSummary(routeSnapshot: RouteSnapshot): String {
        val stopCount = routeSnapshot.stops.size
        val transferCount = routeSnapshot.transfers.size
        val stopLabel = if (stopCount == 1) "1 stop" else "$stopCount stops"
        val transferLabel = if (transferCount == 1) "1 transfer" else "$transferCount transfers"
        return "$stopLabel · $transferLabel"
    }
}
