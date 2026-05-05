package com.nearwake.feature.walkfinish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.CompleteTripUseCase
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.domain.location.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WalkFinishUiState(
    val tripId: String = "",
    val destinationName: String = "Final walk",
    val destinationAddress: String = "",
    val distanceLabel: String = "Distance unavailable",
    val headingLabel: String = "Heading unavailable",
    val instructionLabel: String = "Walk toward the destination and confirm when you arrive.",
    val arrivalHint: String = "NearWake will consider you arrived once you are within about 30m.",
    val canConfirmArrival: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class WalkFinishViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    private val completeTrip: CompleteTripUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(WalkFinishUiState(tripId = tripId))
    val state: StateFlow<WalkFinishUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val place = trip?.destinationId?.let { destinationId ->
                    places.firstOrNull { it.id == destinationId }
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

                WalkFinishUiState(
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
                    headingLabel = heading?.let { "Head $it" } ?: "Heading unavailable",
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
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun confirmArrival(onArrived: () -> Unit) {
        if (!mutableState.value.canConfirmArrival) return
        viewModelScope.launch {
            completeTrip()
            onArrived()
        }
    }

    fun confirmArrivalAndShare(onReadyToShare: (String) -> Unit) {
        if (!mutableState.value.canConfirmArrival) return
        viewModelScope.launch {
            completeTrip()
            onReadyToShare(tripId)
        }
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
        const val TRIP_ID_ARG = "tripId"
        private const val EARTH_RADIUS_METERS = 6_371_000.0
        private const val ARRIVAL_RADIUS_METERS = 30.0
    }

    private suspend fun completeTrip() = completeTrip(tripId)
}
