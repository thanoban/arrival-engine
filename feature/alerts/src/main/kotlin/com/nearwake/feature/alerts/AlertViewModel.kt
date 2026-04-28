package com.nearwake.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.application.monitoring.StopTripMonitoringUseCase
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
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

data class AlertUiState(
    val tripId: String = "",
    val destinationName: String = "Arrival alert",
    val etaLabel: String = "~8 min away",
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    private val stopTripMonitoring: StopTripMonitoringUseCase,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(AlertUiState(tripId = tripId))
    val state: StateFlow<AlertUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
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
        scope.launch {
            stopTripMonitoring()
            onDismissed(tripId)
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
