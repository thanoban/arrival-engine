package com.nearwake.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AlertUiState(
    val tripId: String = "",
    val destinationName: String = "Arrival alert",
    val etaLabel: String = "~8 min away",
    val errorMessage: String? = null,
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(AlertUiState(tripId = tripId))
    val state: StateFlow<AlertUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val place = trip?.destinationId?.let { destinationId -> places.firstOrNull { it.id == destinationId } }
                AlertUiState(
                    tripId = tripId,
                    destinationName = place?.name ?: "Arrival alert",
                    etaLabel = session?.lastEtaMinutes?.let { "~$it min away" } ?: "You are close",
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun enterWalkFinish(onDismissed: (String) -> Unit) {
        viewModelScope.launch {
            stopTripMonitoring()
            onDismissed(tripId)
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
