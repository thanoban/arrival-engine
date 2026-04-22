package com.nearwake.feature.tripsetup

import androidx.lifecycle.ViewModel
import com.nearwake.domain.trip.model.AlertIntensity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TripSetupUiState(
    val destinationName: String = "Central Station",
    val etaLabel: String = "~35 min",
    val alertLeadMinutes: Int = 10,
    val alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val backgroundMonitoringEnabled: Boolean = true,
)

class TripSetupViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(TripSetupUiState())
    val state: StateFlow<TripSetupUiState> = mutableState.asStateFlow()

    fun selectLeadMinutes(minutes: Int) {
        mutableState.value = mutableState.value.copy(alertLeadMinutes = minutes)
    }

    fun selectIntensity(intensity: AlertIntensity) {
        mutableState.value = mutableState.value.copy(alertIntensity = intensity)
    }
}
