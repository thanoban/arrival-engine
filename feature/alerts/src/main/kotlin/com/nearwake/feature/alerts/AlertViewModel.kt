package com.nearwake.feature.alerts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AlertUiState(
    val destinationName: String = "Central Station",
    val etaLabel: String = "~8 min away",
)

class AlertViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(AlertUiState())
    val state: StateFlow<AlertUiState> = mutableState.asStateFlow()
}
