package com.nearwake.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.monitoring.AcknowledgeTripAlertUseCase
import com.nearwake.application.trip.ObserveAlertUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertUiState(
    val tripId: String = "",
    val destinationName: String = "Arrival alert",
    val etaMinutes: Int? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAlert: ObserveAlertUseCase,
    private val acknowledgeTripAlert: AcknowledgeTripAlertUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(AlertUiState(tripId = tripId))
    val state: StateFlow<AlertUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeAlert(tripId).collect { alert ->
                mutableState.value = AlertUiState(
                    tripId = alert.tripId,
                    destinationName = alert.destinationName,
                    etaMinutes = alert.etaMinutes,
                )
            }
        }
    }

    fun enterWalkFinish(onDismissed: (String) -> Unit) {
        viewModelScope.launch {
            acknowledgeTripAlert(tripId)
            onDismissed(tripId)
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
