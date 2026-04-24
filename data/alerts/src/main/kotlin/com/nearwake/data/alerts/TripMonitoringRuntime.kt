package com.nearwake.data.alerts

import com.nearwake.domain.location.model.GeofenceSpec
import com.nearwake.domain.location.model.GeofenceType
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.trip.engine.AlertStageEvaluator
import com.nearwake.domain.trip.engine.TripEngine
import com.nearwake.domain.trip.engine.TripEngineResult
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class MonitoredTripContext(
    val tripId: String,
    val destinationName: String,
    val alertIntensity: AlertIntensity,
    val alertMode: AlertMode,
    val destination: LatLng,
    val tripRule: TripRule,
    val geofenceIds: List<String>,
    val hasCachedRoute: Boolean,
    val routeSnapshot: RouteSnapshot? = null,
    val initialEtaMinutes: Int? = null,
)

enum class GeofenceSignal {
    APPROACH,
    DESTINATION,
}

data class TripMonitoringUpdate(
    val session: TripSession,
    val distanceMeters: Double,
    val etaMinutes: Int?,
    val engineResult: TripEngineResult? = null,
)

@Singleton
class TripMonitoringRuntime @Inject constructor(
    private val tripEngine: TripEngine,
) {
    private val alertStageEvaluator = AlertStageEvaluator()

    fun buildContext(
        tripId: String,
        destinationName: String = "Destination",
        alertLeadMinutes: Int,
        alertIntensity: AlertIntensity,
        alertMode: AlertMode = AlertMode.ACTIVE,
        destination: LatLng,
        hasCachedRoute: Boolean,
        routeSnapshot: RouteSnapshot? = null,
        initialEtaMinutes: Int? = null,
    ): MonitoredTripContext =
        MonitoredTripContext(
            tripId = tripId,
            destinationName = destinationName,
            alertIntensity = alertIntensity,
            alertMode = alertMode,
            destination = destination,
            tripRule = TripRule(alertLeadMinutes = alertLeadMinutes),
            geofenceIds = buildGeofenceIds(tripId),
            hasCachedRoute = hasCachedRoute,
            routeSnapshot = routeSnapshot,
            initialEtaMinutes = initialEtaMinutes,
        )

    fun buildGeofences(context: MonitoredTripContext): List<GeofenceSpec> =
        listOf(
            GeofenceSpec(
                id = context.geofenceIds[0],
                center = context.destination,
                radiusMeters = context.tripRule.approachRadiusMeters,
                type = GeofenceType.APPROACH,
            ),
            GeofenceSpec(
                id = context.geofenceIds[1],
                center = context.destination,
                radiusMeters = context.tripRule.destinationRadiusMeters,
                type = GeofenceType.DESTINATION,
            ),
        )

    fun classifySignal(geofenceIds: List<String>): GeofenceSignal? =
        when {
            geofenceIds.any { it.endsWith(DESTINATION_SUFFIX) } -> GeofenceSignal.DESTINATION
            geofenceIds.any { it.endsWith(APPROACH_SUFFIX) } -> GeofenceSignal.APPROACH
            else -> null
        }

    fun applyLocationUpdate(
        context: MonitoredTripContext,
        session: TripSession,
        location: LatLng,
        etaMinutes: Int?,
        destinationGeofenceEntered: Boolean = false,
    ): TripMonitoringUpdate {
        val distanceMeters = distanceMeters(location, context.destination)
        val previousDistanceMeters = previousDistanceFromDestination(session, context.destination)
        val persistedEta = etaMinutes ?: session.lastEtaMinutes
        val seededSession = session.copy(
            lastKnownLat = location.lat,
            lastKnownLng = location.lng,
            lastEtaMinutes = persistedEta,
            alertStage = alertStageEvaluator.evaluate(
                currentStage = session.alertStage,
                tripState = session.state,
                rule = context.tripRule,
                alertMode = context.alertMode,
                confidence = session.confidence,
                etaMinutes = persistedEta,
                distanceMeters = distanceMeters,
                destinationGeofenceEntered = destinationGeofenceEntered,
            ),
        )

        val engineResult = when {
            destinationGeofenceEntered -> tripEngine.evaluateAlert(
                session = seededSession.ensureApproachState(),
                tripRule = context.tripRule,
                distanceMeters = distanceMeters,
                previousDistanceMeters = previousDistanceMeters,
                destinationGeofenceEntered = true,
            )

            seededSession.state == TripState.MonitoringLowPower -> tripEngine.evaluateApproach(
                session = seededSession,
                tripRule = context.tripRule,
                etaMinutes = persistedEta,
                distanceMeters = distanceMeters,
            )

            seededSession.state == TripState.MonitoringApproach ||
                seededSession.state == TripState.Recovery -> tripEngine.evaluateAlert(
                    session = seededSession,
                    tripRule = context.tripRule,
                    distanceMeters = distanceMeters,
                    previousDistanceMeters = previousDistanceMeters,
                    destinationGeofenceEntered = false,
                )

            else -> null
        }

        return if (engineResult == null) {
            TripMonitoringUpdate(
                session = seededSession,
                distanceMeters = distanceMeters,
                etaMinutes = persistedEta,
            )
        } else {
            val sessionWithTracking = engineResult.session.copy(
                lastKnownLat = location.lat,
                lastKnownLng = location.lng,
                lastEtaMinutes = persistedEta,
                alertStage = alertStageEvaluator.evaluate(
                    currentStage = engineResult.session.alertStage,
                    tripState = engineResult.session.state,
                    rule = context.tripRule,
                    alertMode = context.alertMode,
                    confidence = engineResult.session.confidence,
                    etaMinutes = persistedEta,
                    distanceMeters = distanceMeters,
                    destinationGeofenceEntered = destinationGeofenceEntered,
                ),
            )
            TripMonitoringUpdate(
                session = sessionWithTracking,
                distanceMeters = distanceMeters,
                etaMinutes = persistedEta,
                engineResult = engineResult.copy(session = sessionWithTracking),
            )
        }
    }

    private fun TripSession.ensureApproachState(): TripSession =
        if (state == TripState.MonitoringApproach || state == TripState.Alerting) {
            this
        } else {
            tripEngine.onEvent(this, com.nearwake.domain.trip.engine.TripEvent.ApproachGeofenceEntered).session
                .copy(
                    lastKnownLat = lastKnownLat,
                    lastKnownLng = lastKnownLng,
                    lastEtaMinutes = lastEtaMinutes,
                )
        }

    private fun previousDistanceFromDestination(
        session: TripSession,
        destination: LatLng,
    ): Double? {
        val lat = session.lastKnownLat ?: return null
        val lng = session.lastKnownLng ?: return null
        return distanceMeters(LatLng(lat = lat, lng = lng), destination)
    }

    private fun distanceMeters(first: LatLng, second: LatLng): Double {
        val latDistance = Math.toRadians(second.lat - first.lat)
        val lngDistance = Math.toRadians(second.lng - first.lng)
        val startLat = Math.toRadians(first.lat)
        val endLat = Math.toRadians(second.lat)

        val haversine = sin(latDistance / 2).pow(2.0) +
            sin(lngDistance / 2).pow(2.0) * cos(startLat) * cos(endLat)
        val arc = 2 * asin(sqrt(haversine))
        return EARTH_RADIUS_METERS * arc
    }

    private fun buildGeofenceIds(tripId: String): List<String> =
        listOf(
            "$tripId$APPROACH_SUFFIX",
            "$tripId$DESTINATION_SUFFIX",
        )

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val APPROACH_SUFFIX = ":approach"
        private const val DESTINATION_SUFFIX = ":destination"
    }
}
