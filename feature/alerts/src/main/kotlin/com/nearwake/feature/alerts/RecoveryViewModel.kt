package com.nearwake.feature.alerts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.application.trip.CompleteTripUseCase
import com.nearwake.application.trip.ObserveRecoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val errorMessage: String? = null,
)

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeRecovery: ObserveRecoveryUseCase,
    private val startTripMonitoring: StartTripMonitoringUseCase,
    private val completeTrip: CompleteTripUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(RecoveryUiState(tripId = tripId))
    val state: StateFlow<RecoveryUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeRecovery(tripId).collect { recovery ->
                mutableState.value = RecoveryUiState(
                    tripId = recovery.tripId,
                    destinationName = recovery.destinationName,
                    missedByLabel = recovery.missedByLabel,
                    routeSummary = recovery.routeSummary,
                    lastEtaLabel = recovery.lastEtaLabel,
                    confidenceLabel = recovery.confidenceLabel,
                    recoveryGuidanceLabel = recovery.recoveryGuidanceLabel,
                    returnStopLabel = recovery.returnStopLabel,
                    walkBackLabel = recovery.walkBackLabel,
                    canResumeMonitoring = recovery.canResumeMonitoring,
                )
            }
        }
    }

    fun resumeMonitoring(onResumed: (String) -> Unit) {
        if (!mutableState.value.canResumeMonitoring) return
        startTripMonitoring(tripId)
        onResumed(tripId)
    }

    fun endTrip(onEnded: () -> Unit) {
        viewModelScope.launch {
            completeTrip(tripId)
            onEnded()
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
