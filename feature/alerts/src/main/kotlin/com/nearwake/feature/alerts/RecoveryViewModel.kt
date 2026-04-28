package com.nearwake.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.isTerminal
import com.nearwake.domain.trip.engine.RecoveryGuidanceMode
import com.nearwake.domain.trip.engine.RecoveryPlanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    val recoveryGuidanceLabel: String = "Resume monitoring and check the next safe stop.",
    val returnStopLabel: String? = null,
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
    private val startTripMonitoring: StartTripMonitoringUseCase,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val recoveryPlanner = RecoveryPlanner()
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
                val currentLocation = session?.lastKnownLat?.let { lat ->
                    session.lastKnownLng?.let { lng ->
                        LatLng(lat = lat, lng = lng)
                    }
                }
                val recoveryPlan = place?.let { destination ->
                    recoveryPlanner.plan(
                        currentLocation = currentLocation,
                        destinationLocation = LatLng(destination.lat, destination.lng),
                        routeSnapshot = routeSnapshot,
                    )
                }
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
                    recoveryGuidanceLabel = when (recoveryPlan?.guidanceMode) {
                        RecoveryGuidanceMode.WALK_BACK -> "You are still close enough to recover on foot."
                        RecoveryGuidanceMode.RETURN_STOP -> "Get off at the next safe moment and head for the nearest return stop."
                        RecoveryGuidanceMode.RESUME_MONITORING, null ->
                            "Resume monitoring and check the next safe stop."
                    },
                    returnStopLabel = recoveryPlan?.returnStopName?.let { stopName ->
                        val distanceLabel = recoveryPlan.returnStopDistanceMeters?.let { distance ->
                            "about ${distance}m away"
                        } ?: "nearby"
                        "Nearest return stop: $stopName ($distanceLabel)"
                    },
                    walkBackLabel = recoveryPlan?.walkBackDistanceMeters?.let { distance ->
                        "You are about ${distance}m from ${place?.name ?: "the destination"}. If it is safe, stop now and walk back."
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
        startTripMonitoring(tripId)
        onResumed(tripId)
    }

    fun endTrip(onEnded: () -> Unit) {
        scope.launch {
            tripDao.getTripById(tripId)?.let { trip ->
                tripDao.upsertTrip(trip.copy(completedAt = Clock.System.now()))
            }
            tripSessionDao.deleteTripSession(tripId)
            stopTripMonitoring()
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
