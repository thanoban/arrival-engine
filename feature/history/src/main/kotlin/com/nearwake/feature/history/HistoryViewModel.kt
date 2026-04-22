package com.nearwake.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.core.database.entity.TripEntity
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

data class TripHistoryItemUiModel(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
    val statusLabel: String,
)

data class HistoryUiState(
    val trips: List<TripHistoryItemUiModel> = emptyList(),
)

data class TripSummaryUiState(
    val destinationName: String = "Trip summary",
    val destinationAddress: String = "",
    val statusLabel: String = "Loading",
    val startedLabel: String = "",
    val alertLeadLabel: String = "",
    val alertIntensityLabel: String = "",
    val monitoringLabel: String = "",
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    tripSessionDao: TripSessionDao,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripDao.observeTrips(),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSessions(),
            ) { trips, places, sessions ->
                val placeMap = places.associateBy { it.id }
                val activeTripIds = sessions.map { it.tripId }.toSet()
                HistoryUiState(
                    trips = trips.map { trip ->
                        trip.toHistoryItem(
                            place = placeMap[trip.destinationId],
                            isActive = trip.id in activeTripIds,
                        )
                    },
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    tripSessionDao: TripSessionDao,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSummaryUiState())
    val state: StateFlow<TripSummaryUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
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
                    alertLeadLabel = trip?.alertLeadMinutes?.let { minutes ->
                        if (minutes == 0) "Alert when nearby" else "Alert $minutes minutes early"
                    }.orEmpty(),
                    alertIntensityLabel = trip?.alertIntensity?.name?.toDisplayLabel().orEmpty(),
                    monitoringLabel = session?.monitoringMode?.name?.toDisplayLabel() ?: "Not monitoring",
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
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
