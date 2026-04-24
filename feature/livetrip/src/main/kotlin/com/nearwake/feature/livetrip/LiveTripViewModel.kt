package com.nearwake.feature.livetrip

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.data.alerts.TripMonitoringService
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class LiveTripUiState(
    val tripId: String = "",
    val destinationName: String = "Live trip",
    val etaLabel: String = "~22 min",
    val routeSummary: String = "Destination-only monitoring",
    val elapsedTimeLabel: String = "",
    val monitoringMode: MonitoringMode = MonitoringMode.GEOFENCE_ONLY,
    val confidence: Confidence = Confidence.HIGH,
    val batteryImpact: String = "Very Low",
    val alertSummary: String = "",
    val alertMode: AlertMode = AlertMode.ACTIVE,
    val alertStage: AlertStage = AlertStage.MONITORING,
    val transferSteps: List<TransferProgressUiState> = emptyList(),
)

@HiltViewModel
class LiveTripViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    private val routingRepository: RoutingRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(LiveTripUiState(tripId = tripId))
    val state: StateFlow<LiveTripUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val routeSnapshot = routingRepository.getCachedRoute(tripId)
                val place = trip?.destinationId?.let { destinationId -> places.firstOrNull { it.id == destinationId } }
                val minutes = session?.lastEtaMinutes ?: 22
                LiveTripUiState(
                    tripId = tripId,
                    destinationName = place?.name ?: "Live trip",
                    etaLabel = "~${minutes} min",
                    routeSummary = routeSnapshot?.let { snapshot ->
                        val stopLabel = if (snapshot.stops.size == 1) "1 stop" else "${snapshot.stops.size} stops"
                        val transferLabel = if (snapshot.transfers.size == 1) "1 transfer" else "${snapshot.transfers.size} transfers"
                        "$stopLabel · $transferLabel"
                    } ?: "Destination-only monitoring",
                    elapsedTimeLabel = trip?.createdAt?.toString()?.replace('T', ' ')?.take(16).orEmpty(),
                    monitoringMode = session?.monitoringMode ?: MonitoringMode.GEOFENCE_ONLY,
                    confidence = session?.confidence ?: Confidence.HIGH,
                    batteryImpact = when (session?.monitoringMode ?: MonitoringMode.GEOFENCE_ONLY) {
                        MonitoringMode.GEOFENCE_ONLY -> "Very Low"
                        MonitoringMode.BALANCED -> "Low"
                        MonitoringMode.PRECISE_BURST -> "Temporary spike"
                    },
                    alertMode = trip?.alertMode ?: AlertMode.ACTIVE,
                    alertStage = session?.alertStage ?: AlertStage.MONITORING,
                    transferSteps = routeSnapshot?.let { snapshot ->
                        buildTransferProgress(
                            routeSnapshot = snapshot,
                            etaMinutes = minutes,
                            destinationName = place?.name ?: "Destination",
                        )
                    }.orEmpty(),
                    alertSummary = trip?.let { configuredTrip ->
                        val lead = if (configuredTrip.alertLeadMinutes == 0) "Nearby" else "${configuredTrip.alertLeadMinutes} min early"
                        "$lead · ${configuredTrip.alertIntensity.name.lowercase().replaceFirstChar(Char::uppercase)} · ${configuredTrip.alertMode.name.lowercase().replaceFirstChar(Char::uppercase)} mode"
                    }.orEmpty(),
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun updateAlertMode(mode: AlertMode) {
        scope.launch {
            val trip = tripDao.getTripById(tripId) ?: return@launch
            tripDao.upsertTrip(trip.copy(alertMode = mode))
        }
    }

    fun cancelTrip(onCancelled: () -> Unit) {
        scope.launch {
            tripSessionDao.deleteTripSession(tripId)
            TripMonitoringService.stop(appContext)
            onCancelled()
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

data class TransferProgressUiState(
    val title: String,
    val subtitle: String,
    val timingLabel: String,
    val status: TransferProgressStatus,
)

enum class TransferProgressStatus {
    Completed,
    Soon,
    Upcoming,
    Final,
}

internal fun buildTransferProgress(
    routeSnapshot: RouteSnapshot,
    etaMinutes: Int,
    destinationName: String,
): List<TransferProgressUiState> {
    val elapsedMinutes = (routeSnapshot.totalDurationMinutes - etaMinutes).coerceAtLeast(0)
    val transferSteps = routeSnapshot.transfers
        .sortedBy { transfer -> transfer.arrivalMinutes }
        .map { transfer ->
            val remainingMinutes = transfer.arrivalMinutes - elapsedMinutes
            when {
                remainingMinutes <= 0 -> TransferProgressUiState(
                    title = "Changed to ${transfer.lineName}",
                    subtitle = transfer.stop.name,
                    timingLabel = "Passed",
                    status = TransferProgressStatus.Completed,
                )

                remainingMinutes <= 3 -> TransferProgressUiState(
                    title = "Change to ${transfer.lineName}",
                    subtitle = transfer.stop.name,
                    timingLabel = if (remainingMinutes <= 1) "Now" else "in $remainingMinutes min",
                    status = TransferProgressStatus.Soon,
                )

                else -> TransferProgressUiState(
                    title = "Change to ${transfer.lineName}",
                    subtitle = transfer.stop.name,
                    timingLabel = "in $remainingMinutes min",
                    status = TransferProgressStatus.Upcoming,
                )
            }
        }

    return transferSteps + TransferProgressUiState(
        title = "Arrive at $destinationName",
        subtitle = "Final stop",
        timingLabel = if (etaMinutes <= 1) "Now" else "in $etaMinutes min",
        status = TransferProgressStatus.Final,
    )
}
