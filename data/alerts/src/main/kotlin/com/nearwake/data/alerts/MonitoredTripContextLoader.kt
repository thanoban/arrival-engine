package com.nearwake.data.alerts

import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.repository.RoutingRepository
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MonitoredTripContextLoader @Inject constructor(
    private val tripDao: TripDao,
    private val savedPlaceDao: SavedPlaceDao,
    private val routingRepository: RoutingRepository,
    private val tripMonitoringRuntime: TripMonitoringRuntime,
    private val diagnosticsLogger: DiagnosticsLogger,
) {
    suspend fun load(
        tripId: String,
        batterySaverMode: Boolean,
    ): MonitoredTripContext? {
        return runCatching {
            val trip = tripDao.getTripById(tripId) ?: return null
            val destination = savedPlaceDao.getSavedPlaceById(trip.destinationId) ?: return null
            val cachedRoute = routingRepository.getCachedRoute(tripId)
            tripMonitoringRuntime.buildContext(
                tripId = trip.id,
                destinationName = destination.name,
                alertLeadMinutes = trip.alertLeadMinutes,
                alertTriggerMode = trip.alertTriggerMode,
                alertDistanceMeters = trip.alertDistanceMeters,
                alertIntensity = trip.alertIntensity,
                alertMode = trip.alertMode,
                destination = LatLng(lat = destination.lat, lng = destination.lng),
                hasCachedRoute = cachedRoute != null,
                routeSnapshot = cachedRoute,
                initialEtaMinutes = cachedRoute?.totalDurationMinutes,
                batterySaverMode = batterySaverMode,
            )
        }.onFailure { error ->
            diagnosticsLogger.log(
                eventType = "monitoring_service_trip_context_failed",
                tripId = tripId,
                payload = buildJsonObject {
                    put("message", error.message.orEmpty())
                },
            )
        }.getOrNull()
    }
}
