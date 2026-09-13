package com.nearwake.application.trip

import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.ports.analytics.NearWakeAnalytics
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import java.util.UUID
import javax.inject.Inject
import kotlinx.datetime.Clock

class RearmTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val locationRepository: LocationRepository,
    private val routingRepository: RoutingRepository,
    private val startTripMonitoring: StartTripMonitoringUseCase,
    private val analytics: NearWakeAnalytics,
) {
    suspend operator fun invoke(sourceTripId: String): String? {
        val sourceTrip = tripLifecycleStore.getTrip(sourceTripId) ?: return null
        val place = tripLifecycleStore.getSavedPlace(sourceTrip.destinationId) ?: return null
        val tripId = UUID.randomUUID().toString()
        val now = Clock.System.now()

        val routeSnapshot = runCatching {
            val origin = locationRepository.getLastKnownLocation()
            if (origin != null) {
                routingRepository.fetchRoute(
                    origin = origin,
                    destination = LatLng(lat = place.lat, lng = place.lng),
                ).getOrNull()
            } else {
                null
            }
        }.onFailure { error ->
            analytics.recordFailure(
                surface = "rearm_trip_route_fetch",
                throwable = error,
                attributes = mapOf("source_trip_id" to sourceTripId),
            )
        }.getOrNull()

        routeSnapshot?.let { snapshot ->
            routingRepository.cacheRouteForTrip(tripId = tripId, routeSnapshot = snapshot)
        }

        tripLifecycleStore.markPlaceUsed(placeId = place.id, usedAt = now)
        tripLifecycleStore.saveTrip(
            SaveTripCommand(
                id = tripId,
                destinationId = place.id,
                alertLeadMinutes = sourceTrip.alertLeadMinutes,
                alertTriggerMode = sourceTrip.alertTriggerMode,
                alertDistanceMeters = sourceTrip.alertDistanceMeters,
                alertIntensity = sourceTrip.alertIntensity,
                alertMode = sourceTrip.alertMode,
                createdAt = now,
            ),
        )
        tripLifecycleStore.saveTripSession(
            SaveTripSessionCommand(
                tripId = tripId,
                state = TripState.Armed,
                monitoringMode = MonitoringMode.GEOFENCE_ONLY,
                confidence = Confidence.HIGH,
                geofenceIds = emptyList(),
                lastEtaMinutes = routeSnapshot?.totalDurationMinutes,
                updatedAt = now,
            ),
        )
        startTripMonitoring(tripId)
        analytics.trackTripArmed(
            mode = sourceTrip.alertMode.name,
            hasTransfers = routeSnapshot?.transfers?.isNotEmpty() == true,
            transferCount = routeSnapshot?.transfers?.size ?: 0,
        )
        return tripId
    }
}
