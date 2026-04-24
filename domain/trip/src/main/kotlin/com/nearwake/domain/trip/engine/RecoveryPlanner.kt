package com.nearwake.domain.trip.engine

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class RecoveryGuidanceMode {
    WALK_BACK,
    RETURN_STOP,
    RESUME_MONITORING,
}

data class RecoveryPlan(
    val guidanceMode: RecoveryGuidanceMode,
    val walkBackDistanceMeters: Int? = null,
    val returnStopName: String? = null,
    val returnStopDistanceMeters: Int? = null,
)

class RecoveryPlanner(
    private val walkBackThresholdMeters: Double = DEFAULT_WALK_BACK_THRESHOLD_METERS,
) {
    fun plan(
        currentLocation: LatLng?,
        destinationLocation: LatLng,
        routeSnapshot: RouteSnapshot?,
    ): RecoveryPlan {
        val current = currentLocation ?: return RecoveryPlan(
            guidanceMode = RecoveryGuidanceMode.RESUME_MONITORING,
        )

        val destinationDistance = distanceMeters(current, destinationLocation)
        if (destinationDistance <= walkBackThresholdMeters) {
            return RecoveryPlan(
                guidanceMode = RecoveryGuidanceMode.WALK_BACK,
                walkBackDistanceMeters = destinationDistance.toInt(),
            )
        }

        val nearestReturnStop = routeSnapshot?.stops?.minByOrNull { stop ->
            distanceMeters(current, LatLng(stop.lat, stop.lng))
        }
        if (nearestReturnStop != null) {
            val stopDistance = distanceMeters(
                current,
                LatLng(nearestReturnStop.lat, nearestReturnStop.lng),
            )
            return RecoveryPlan(
                guidanceMode = RecoveryGuidanceMode.RETURN_STOP,
                returnStopName = nearestReturnStop.name,
                returnStopDistanceMeters = stopDistance.toInt(),
            )
        }

        return RecoveryPlan(
            guidanceMode = RecoveryGuidanceMode.RESUME_MONITORING,
        )
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

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val DEFAULT_WALK_BACK_THRESHOLD_METERS = 400.0
    }
}
