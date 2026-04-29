package com.nearwake.application.trip

import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.ports.persistence.SaveTripCommand
import com.nearwake.ports.persistence.SaveTripSessionCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import java.util.UUID
import javax.inject.Inject
import kotlinx.datetime.Clock

data class StartTripRequest(
    val placeId: String,
    val alertLeadMinutes: Int,
    val alertTriggerMode: AlertTriggerMode,
    val alertDistanceMeters: Int,
    val alertIntensity: AlertIntensity,
    val alertMode: AlertMode,
    val previewRouteSnapshot: RouteSnapshot? = null,
)

class StartTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val routingRepository: RoutingRepository,
    private val startTripMonitoring: StartTripMonitoringUseCase,
) {
    suspend operator fun invoke(request: StartTripRequest): String? {
        val place = tripLifecycleStore.getSavedPlace(request.placeId) ?: return null
        val tripId = UUID.randomUUID().toString()
        val now = Clock.System.now()

        request.previewRouteSnapshot?.let { routeSnapshot ->
            routingRepository.cacheRouteForTrip(tripId = tripId, routeSnapshot = routeSnapshot)
        }

        tripLifecycleStore.markPlaceUsed(placeId = place.id, usedAt = now)
        tripLifecycleStore.saveTrip(
            SaveTripCommand(
                id = tripId,
                destinationId = place.id,
                alertLeadMinutes = request.alertLeadMinutes,
                alertTriggerMode = request.alertTriggerMode,
                alertDistanceMeters = request.alertDistanceMeters,
                alertIntensity = request.alertIntensity,
                alertMode = request.alertMode,
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
                lastEtaMinutes = request.previewRouteSnapshot?.totalDurationMinutes ?: 35,
                updatedAt = now,
            ),
        )
        startTripMonitoring(tripId)
        return tripId
    }
}
