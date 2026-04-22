package com.nearwake.feature.diagnostics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiagnosticsUiState(
    val stateLabel: String = "Armed",
    val registeredGeofences: List<String> = listOf("approach-central-station", "destination-central-station"),
    val recentEvents: List<String> = listOf(
        "trip_started",
        "geofence_registered",
        "monitoring_mode_changed",
    ),
)

class DiagnosticsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(DiagnosticsUiState())
    val state: StateFlow<DiagnosticsUiState> = mutableState.asStateFlow()
}
