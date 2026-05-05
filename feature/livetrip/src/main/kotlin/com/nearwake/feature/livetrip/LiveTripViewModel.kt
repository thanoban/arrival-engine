package com.nearwake.feature.livetrip

import android.content.Context
import android.os.BatteryManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.CancelTripUseCase
import com.nearwake.application.trip.LiveTripTransferStatus
import com.nearwake.application.trip.ObserveLiveTripUseCase
import com.nearwake.application.trip.UpdateTripAlertModeUseCase
import com.nearwake.domain.routing.model.RouteSignalQuality
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val batterySaverActive: Boolean = false,
    val alertSummary: String = "",
    val alertMode: AlertMode = AlertMode.ACTIVE,
    val alertStage: AlertStage = AlertStage.MONITORING,
    val transferSteps: List<TransferProgressUiState> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class LiveTripViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeLiveTrip: ObserveLiveTripUseCase,
    @ApplicationContext private val appContext: Context,
    private val updateTripAlertMode: UpdateTripAlertModeUseCase,
    private val cancelTrip: CancelTripUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(LiveTripUiState(tripId = tripId))
    val state: StateFlow<LiveTripUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeLiveTrip(tripId).collect { liveTrip ->
                mutableState.value = LiveTripUiState(
                    tripId = liveTrip.tripId,
                    destinationName = liveTrip.destinationName,
                    etaLabel = liveTrip.etaLabel,
                    routeSummary = liveTrip.routeSummary,
                    elapsedTimeLabel = liveTrip.elapsedTimeLabel,
                    monitoringMode = liveTrip.monitoringMode,
                    confidence = liveTrip.confidence,
                    batteryImpact = when (liveTrip.monitoringMode) {
                        MonitoringMode.GEOFENCE_ONLY -> "Very Low"
                        MonitoringMode.BALANCED -> "Low"
                        MonitoringMode.PRECISE_BURST -> "Temporary spike"
                    },
                    batterySaverActive = readBatteryPercent() < BATTERY_SAVER_THRESHOLD,
                    alertSummary = liveTrip.alertSummary,
                    alertMode = liveTrip.alertMode,
                    alertStage = liveTrip.alertStage,
                    transferSteps = liveTrip.transferSteps.map { step ->
                        TransferProgressUiState(
                            title = step.title,
                            subtitle = step.subtitle,
                            timingLabel = step.timingLabel,
                            status = when (step.status) {
                                LiveTripTransferStatus.Completed -> TransferProgressStatus.Completed
                                LiveTripTransferStatus.Soon -> TransferProgressStatus.Soon
                                LiveTripTransferStatus.Upcoming -> TransferProgressStatus.Upcoming
                                LiveTripTransferStatus.Final -> TransferProgressStatus.Final
                            },
                            signalQuality = step.signalQuality,
                        )
                    },
                    errorMessage = null,
                )
            }
        }
    }

    fun updateAlertMode(mode: AlertMode) {
        viewModelScope.launch {
            updateTripAlertMode(tripId, mode)
        }
    }

    fun cancelTrip(onCancelled: () -> Unit) {
        viewModelScope.launch {
            cancelTrip(tripId)
            onCancelled()
        }
    }

    private fun readBatteryPercent(): Int {
        val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
        private const val BATTERY_SAVER_THRESHOLD = 20
    }
}

data class TransferProgressUiState(
    val title: String,
    val subtitle: String,
    val timingLabel: String,
    val status: TransferProgressStatus,
    val signalQuality: RouteSignalQuality = RouteSignalQuality.HIGH,
)

enum class TransferProgressStatus {
    Completed,
    Soon,
    Upcoming,
    Final,
}
