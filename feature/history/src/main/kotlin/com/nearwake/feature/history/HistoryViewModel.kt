package com.nearwake.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertTriggerMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nearwake.core.database.entity.TripSessionEntity

data class TripHistoryItemUiModel(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
    val statusLabel: String,
)

data class HistoryUiState(
    val trips: List<TripHistoryItemUiModel> = emptyList(),
    val exportCsvText: String? = null,
    val errorMessage: String? = null,
)

data class TripSummaryUiState(
    val destinationName: String = "Trip summary",
    val destinationAddress: String = "",
    val statusLabel: String = "Loading",
    val startedLabel: String = "",
    val alertLeadLabel: String = "",
    val alertIntensityLabel: String = "",
    val monitoringLabel: String = "",
    val routeSummary: String = "Destination-only monitoring",
    val etaLabel: String = "",
    val confidenceLabel: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    tripSessionDao: TripSessionDao,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = mutableState.asStateFlow()

    private var latestTrips: List<TripEntity> = emptyList()
    private var latestPlaces: Map<String, SavedPlaceEntity> = emptyMap()
    private var latestSessions: Map<String, TripSessionEntity> = emptyMap()

    init {
        viewModelScope.launch {
            combine(
                tripDao.observeTrips(),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSessions(),
            ) { trips, places, sessions ->
                latestTrips = trips
                latestPlaces = places.associateBy { it.id }
                latestSessions = sessions.associateBy { it.tripId }
                val activeTripIds = sessions.map { it.tripId }.toSet()
                HistoryUiState(
                    trips = trips.map { trip ->
                        trip.toHistoryItem(
                            place = latestPlaces[trip.destinationId],
                            isActive = trip.id in activeTripIds,
                        )
                    },
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun exportTrips() {
        val csv = buildTripCsv(
            trips = latestTrips,
            places = latestPlaces,
            sessions = latestSessions,
        )
        mutableState.value = mutableState.value.copy(exportCsvText = csv)
    }

    fun clearExport() {
        mutableState.value = mutableState.value.copy(exportCsvText = null)
    }
}

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    tripSessionDao: TripSessionDao,
    private val routingRepository: RoutingRepository,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSummaryUiState())
    val state: StateFlow<TripSummaryUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val routeSnapshot = routingRepository.getCachedRoute(tripId)
                val place = trip?.destinationId?.let { destinationId -> places.firstOrNull { it.id == destinationId } }
                TripSummaryUiState(
                    destinationName = place?.name ?: "Unknown destination",
                    destinationAddress = place?.address.orEmpty(),
                    statusLabel = when {
                        trip == null -> "Missing"
                        trip.completedAt != null -> "Completed"
                        session != null -> "Monitoring in progress"
                        else -> "Ended early"
                    },
                    startedLabel = trip?.createdAt?.toReadableLabel().orEmpty(),
                    alertLeadLabel = trip?.toAlertPreferenceLabel().orEmpty(),
                    alertIntensityLabel = trip?.alertIntensity?.name?.toDisplayLabel().orEmpty(),
                    monitoringLabel = session?.monitoringMode?.name?.toDisplayLabel() ?: "Not monitoring",
                    routeSummary = routeSnapshot?.toRouteSummary() ?: "Destination-only monitoring",
                    etaLabel = session?.lastEtaMinutes?.let { "~$it min at last check" }.orEmpty(),
                    confidenceLabel = session?.confidence?.name?.toDisplayLabel() ?: "No confidence data",
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}

private fun TripEntity.toHistoryItem(
    place: SavedPlaceEntity?,
    isActive: Boolean,
): TripHistoryItemUiModel =
    TripHistoryItemUiModel(
        tripId = id,
        destinationName = place?.name ?: "Unknown destination",
        subtitle = place?.address ?: "Saved destination",
        statusLabel = when {
            completedAt != null -> "Completed"
            isActive -> "Monitoring now"
            else -> "Ended early"
        },
    )

private fun String.toDisplayLabel(): String =
    lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)

private fun kotlinx.datetime.Instant.toReadableLabel(): String =
    toString().replace('T', ' ').take(16)

private fun TripEntity.toAlertPreferenceLabel(): String = when (alertTriggerMode) {
    AlertTriggerMode.TIME ->
        if (alertLeadMinutes == 0) "Alert when nearby" else "Alert $alertLeadMinutes minutes early"
    AlertTriggerMode.DISTANCE -> "Alert ${alertDistanceMeters.toDistanceLabel()} away"
    AlertTriggerMode.BOTH -> buildString {
        append("Alert ")
        append(if (alertLeadMinutes == 0) "nearby" else "$alertLeadMinutes minutes early")
        append(" or ")
        append(alertDistanceMeters.toDistanceLabel())
        append(" away")
    }
}

private fun Int.toDistanceLabel(): String =
    if (this >= 1000) {
        val kilometers = this / 1000.0
        if (kilometers % 1.0 == 0.0) "${kilometers.toInt()} km" else "${kilometers} km"
    } else {
        "$this m"
    }

private fun com.nearwake.domain.routing.model.RouteSnapshot.toRouteSummary(): String {
    val stopLabel = if (stops.size == 1) "1 stop" else "${stops.size} stops"
    val transferLabel = if (transfers.size == 1) "1 transfer" else "${transfers.size} transfers"
    return "$stopLabel · $transferLabel"
}
