package com.nearwake.domain.trip.engine

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class BoardingAlignment {
    ALIGNED,
    WRONG_DIRECTION,
    UNDETERMINED,
}

data class BoardingValidationResult(
    val alignment: BoardingAlignment,
    val expectedHeadingDegrees: Double? = null,
    val actualHeadingDegrees: Double? = null,
    val headingDeltaDegrees: Double? = null,
)

class BoardingValidator(
    private val movementThresholdMeters: Double = DEFAULT_MOVEMENT_THRESHOLD_METERS,
    private val wrongDirectionThresholdDegrees: Double = DEFAULT_WRONG_DIRECTION_THRESHOLD_DEGREES,
) {
    fun evaluate(
        routeSnapshot: RouteSnapshot,
        previousLocation: LatLng?,
        currentLocation: LatLng,
    ): BoardingValidationResult {
        val from = previousLocation ?: return BoardingValidationResult(BoardingAlignment.UNDETERMINED)
        val orderedStops = routeSnapshot.stops.sortedBy { stop -> stop.order }
        val firstLeg = orderedStops.zipWithNext().firstOrNull()
            ?: return BoardingValidationResult(BoardingAlignment.UNDETERMINED)

        val movedDistanceMeters = distanceMeters(from, currentLocation)
        if (movedDistanceMeters < movementThresholdMeters) {
            return BoardingValidationResult(BoardingAlignment.UNDETERMINED)
        }

        val expectedHeading = bearingDegrees(
            start = LatLng(firstLeg.first.lat, firstLeg.first.lng),
            end = LatLng(firstLeg.second.lat, firstLeg.second.lng),
        )
        val actualHeading = bearingDegrees(
            start = from,
            end = currentLocation,
        )
        val headingDelta = headingDifferenceDegrees(expectedHeading, actualHeading)

        return BoardingValidationResult(
            alignment = if (headingDelta >= wrongDirectionThresholdDegrees) {
                BoardingAlignment.WRONG_DIRECTION
            } else {
                BoardingAlignment.ALIGNED
            },
            expectedHeadingDegrees = expectedHeading,
            actualHeadingDegrees = actualHeading,
            headingDeltaDegrees = headingDelta,
        )
    }

    private fun bearingDegrees(start: LatLng, end: LatLng): Double {
        val startLat = Math.toRadians(start.lat)
        val endLat = Math.toRadians(end.lat)
        val deltaLng = Math.toRadians(end.lng - start.lng)

        val y = sin(deltaLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) -
            sin(startLat) * cos(endLat) * cos(deltaLng)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    private fun headingDifferenceDegrees(first: Double, second: Double): Double {
        val delta = abs(first - second) % 360.0
        return if (delta > 180.0) 360.0 - delta else delta
    }

    private fun distanceMeters(first: LatLng, second: LatLng): Double {
        val latDistance = Math.toRadians(second.lat - first.lat)
        val lngDistance = Math.toRadians(second.lng - first.lng)
        val startLat = Math.toRadians(first.lat)
        val endLat = Math.toRadians(second.lat)

        val haversine = sin(latDistance / 2).pow(2.0) +
            sin(lngDistance / 2).pow(2.0) * cos(startLat) * cos(endLat)
        val arc = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))
        return EARTH_RADIUS_METERS * arc
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val DEFAULT_MOVEMENT_THRESHOLD_METERS = 120.0
        private const val DEFAULT_WRONG_DIRECTION_THRESHOLD_DEGREES = 70.0
    }
}
