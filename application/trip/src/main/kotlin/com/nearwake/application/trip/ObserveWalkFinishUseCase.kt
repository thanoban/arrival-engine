package com.nearwake.application.trip

import com.nearwake.domain.location.model.LatLng
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class WalkFinishPresentation(
    val tripId: String,
    val destinationName: String,
    val destinationAddress: String,
    val distanceLabel: String,
    val headingLabel: String,
    val instructionLabel: String,
    val arrivalHint: String,
    val canConfirmArrival: Boolean,
)

class ObserveWalkFinishUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(tripId: String): Flow<WalkFinishPresentation> =
        tripLifecycleStore.observeHomeSnapshot().map { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val session = snapshot.sessions.firstOrNull { persistedSession -> persistedSession.tripId == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            val destination = place?.let { LatLng(lat = it.lat, lng = it.lng) }
            val current = session?.lastKnownLat?.let { lat ->
                session.lastKnownLng?.let { lng ->
                    LatLng(lat = lat, lng = lng)
                }
            }
            val distanceMeters = if (current != null && destination != null) {
                distanceMeters(current, destination)
            } else {
                null
            }
            val heading = if (current != null && destination != null) {
                cardinalDirection(current, destination)
            } else {
                null
            }

            WalkFinishPresentation(
                tripId = tripId,
                destinationName = place?.name ?: "Final walk",
                destinationAddress = place?.address.orEmpty(),
                distanceLabel = distanceMeters?.let { distance ->
                    when {
                        distance < 30 -> "You are about ${distance.roundToInt()}m away"
                        distance < 1000 -> "${distance.roundToInt()}m remaining"
                        else -> String.format("%.1f km remaining", distance / 1000.0)
                    }
                } ?: "Distance unavailable",
                headingLabel = heading?.let { direction -> "Head $direction" } ?: "Heading unavailable",
                instructionLabel = when {
                    distanceMeters == null -> "Final guidance is limited, but your destination is saved below."
                    distanceMeters <= ARRIVAL_RADIUS_METERS ->
                        "You should be at the destination now. Confirm arrival when you are safely there."
                    heading != null ->
                        "Walk $heading toward ${place?.name ?: "the destination"} until you are within 30m."
                    else -> "Walk toward ${place?.name ?: "the destination"} and confirm when you arrive."
                },
                arrivalHint = if (distanceMeters != null && distanceMeters <= ARRIVAL_RADIUS_METERS) {
                    "NearWake thinks you have reached the final destination."
                } else {
                    "NearWake will consider you arrived once you are within about 30m."
                },
                canConfirmArrival = place != null,
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

    private fun cardinalDirection(first: LatLng, second: LatLng): String {
        val startLat = Math.toRadians(first.lat)
        val endLat = Math.toRadians(second.lat)
        val deltaLng = Math.toRadians(second.lng - first.lng)
        val y = sin(deltaLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) -
            sin(startLat) * cos(endLat) * cos(deltaLng)
        val bearing = (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
        val sectors = listOf(
            "north",
            "north-east",
            "east",
            "south-east",
            "south",
            "south-west",
            "west",
            "north-west",
        )
        val index = ((bearing + 22.5) / 45.0).toInt() % sectors.size
        return sectors[index]
    }

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val ARRIVAL_RADIUS_METERS = 30.0
    }
}
