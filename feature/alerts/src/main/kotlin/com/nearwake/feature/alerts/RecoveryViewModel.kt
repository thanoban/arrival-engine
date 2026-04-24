package com.nearwake.feature.alerts

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.data.alerts.TripMonitoringService
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.isTerminal
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class RecoveryUiState(
    val tripId: String = "",
    val destinationName: String = "Recovery",
    val missedByLabel: String = "moments ago",
    val routeSummary: String = "Destination-only monitoring",
    val lastEtaLabel: String = "Last ETA unavailable",
    val confidenceLabel: String = "Confidence unknown",
    val walkBackLabel: String? = null,
    val canResumeMonitoring: Boolean = false,
)

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    private val routingRepository: RoutingRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(RecoveryUiState(tripId = tripId))
    val state: StateFlow<RecoveryUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val place = trip?.destinationId?.let { destinationId -> places.firstOrNull { it.id == destinationId } }
                val routeSnapshot = routingRepository.getCachedRoute(tripId)
                RecoveryUiState(
                    tripId = tripId,
                    destinationName = place?.name ?: "Recovery",
                    missedByLabel = session?.updatedAt?.toString()?.replace('T', ' ')?.take(16) ?: "moments ago",
                    routeSummary = routeSnapshot?.let { snapshot ->
                        val stopLabel = if (snapshot.stops.size == 1) "1 stop" else "${snapshot.stops.size} stops"
                        val transferLabel = if (snapshot.transfers.size == 1) "1 transfer" else "${snapshot.transfers.size} transfers"
                        "$stopLabel · $transferLabel"
                    } ?: "Destination-only monitoring",
                    lastEtaLabel = session?.lastEtaMinutes?.let { "~$it min at last check" } ?: "Last ETA unavailable",
                    confidenceLabel = when (session?.confidence ?: Confidence.HIGH) {
                        Confidence.HIGH -> "High confidence"
                        Confidence.DEGRADED -> "Medium confidence"
                        Confidence.OFFLINE -> "Low confidence"
                    },
                    walkBackLabel = session?.let { currentSession ->
                        val lastKnownLat = currentSession.lastKnownLat
                        val lastKnownLng = currentSession.lastKnownLng
                        if (place != null && lastKnownLat != null && lastKnownLng != null) {
                        val distance = distanceMeters(
                            firstLat = lastKnownLat,
                            firstLng = lastKnownLng,
                            secondLat = place.lat,
                            secondLng = place.lng,
                        )
                        if (distance < 400.0) {
                            "You are about ${distance.toInt()}m from ${place.name}. If it is safe, stop now and walk back."
                        } else {
                            null
                        }
                        } else {
                            null
                        }
                    },
                    canResumeMonitoring = session != null && !session.state.isTerminal,
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun resumeMonitoring(onResumed: (String) -> Unit) {
        if (!mutableState.value.canResumeMonitoring) return
        TripMonitoringService.start(appContext, tripId)
        onResumed(tripId)
    }

    fun endTrip(onEnded: () -> Unit) {
        scope.launch {
            tripDao.getTripById(tripId)?.let { trip ->
                tripDao.upsertTrip(trip.copy(completedAt = Clock.System.now()))
            }
            tripSessionDao.deleteTripSession(tripId)
            TripMonitoringService.stop(appContext)
            onEnded()
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}

private fun distanceMeters(
    firstLat: Double,
    firstLng: Double,
    secondLat: Double,
    secondLng: Double,
): Double {
    val latDistance = Math.toRadians(secondLat - firstLat)
    val lngDistance = Math.toRadians(secondLng - firstLng)
    val startLat = Math.toRadians(firstLat)
    val endLat = Math.toRadians(secondLat)

    val haversine = sin(latDistance / 2).pow(2.0) +
        sin(lngDistance / 2).pow(2.0) * cos(startLat) * cos(endLat)
    val arc = 2 * asin(sqrt(haversine))
    return 6_371_000.0 * arc
}
