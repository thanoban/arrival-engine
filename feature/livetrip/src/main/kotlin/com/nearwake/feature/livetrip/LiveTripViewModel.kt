package com.nearwake.feature.livetrip

import androidx.lifecycle.ViewModel
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveTripUiState(
    val destinationName: String = "Central Station",
    val etaLabel: String = "~22 min",
    val elapsedTimeLabel: String = "00:12:33",
    val monitoringMode: MonitoringMode = MonitoringMode.BALANCED,
    val confidence: Confidence = Confidence.HIGH,
    val batteryImpact: String = "Very Low",
    val alertSummary: String = "10 min before · Standard",
)

class LiveTripViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(LiveTripUiState())
    val state: StateFlow<LiveTripUiState> = mutableState.asStateFlow()
}
