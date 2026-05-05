package com.nearwake.feature.walkfinish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.CompleteTripUseCase
import com.nearwake.application.trip.ObserveWalkFinishUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    observeWalkFinish: ObserveWalkFinishUseCase,
    private val completeTrip: CompleteTripUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(WalkFinishUiState(tripId = tripId))
    val state: StateFlow<WalkFinishUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeWalkFinish(tripId).collect { walkFinish ->
                mutableState.value = WalkFinishUiState(
                    tripId = walkFinish.tripId,
                    destinationName = walkFinish.destinationName,
                    destinationAddress = walkFinish.destinationAddress,
                    distanceLabel = walkFinish.distanceLabel,
                    headingLabel = walkFinish.headingLabel,
                    instructionLabel = walkFinish.instructionLabel,
                    arrivalHint = walkFinish.arrivalHint,
                    canConfirmArrival = walkFinish.canConfirmArrival,
                )
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

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }

    private suspend fun completeTrip() = completeTrip(tripId)
}
