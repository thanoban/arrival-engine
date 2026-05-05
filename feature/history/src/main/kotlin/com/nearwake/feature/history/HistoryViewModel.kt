package com.nearwake.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.BuildTripHistoryCsvUseCase
import com.nearwake.application.trip.ObserveTripHistoryUseCase
import com.nearwake.application.trip.ObserveTripSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    observeTripHistory: ObserveTripHistoryUseCase,
    private val buildTripHistoryCsv: BuildTripHistoryCsvUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTripHistory().collect { summary ->
                mutableState.value = mutableState.value.copy(
                    trips = summary.trips.map { trip ->
                        TripHistoryItemUiModel(
                            tripId = trip.tripId,
                            destinationName = trip.destinationName,
                            subtitle = trip.subtitle,
                            statusLabel = trip.statusLabel,
                        )
                    },
                    errorMessage = null,
                )
            }
        }
    }

    fun exportTrips() {
        viewModelScope.launch {
            runCatching { buildTripHistoryCsv() }
                .onSuccess { csv ->
                    mutableState.value = mutableState.value.copy(
                        exportCsvText = csv,
                        errorMessage = null,
                    )
                }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        errorMessage = error.message ?: "Unable to export trip history.",
                    )
                }
        }
    }

    fun clearExport() {
        mutableState.value = mutableState.value.copy(exportCsvText = null)
    }
}

@HiltViewModel
class TripSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTripSummary: ObserveTripSummaryUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSummaryUiState())
    val state: StateFlow<TripSummaryUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTripSummary(tripId).collect { summary ->
                mutableState.value = TripSummaryUiState(
                    destinationName = summary.destinationName,
                    destinationAddress = summary.destinationAddress,
                    statusLabel = summary.statusLabel,
                    startedLabel = summary.startedLabel,
                    alertLeadLabel = summary.alertLeadLabel,
                    alertIntensityLabel = summary.alertIntensityLabel,
                    monitoringLabel = summary.monitoringLabel,
                    routeSummary = summary.routeSummary,
                    etaLabel = summary.etaLabel,
                    confidenceLabel = summary.confidenceLabel,
                    errorMessage = null,
                )
            }
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
