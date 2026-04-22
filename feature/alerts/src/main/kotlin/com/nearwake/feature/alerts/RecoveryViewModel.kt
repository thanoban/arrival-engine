package com.nearwake.feature.alerts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecoveryUiState(
    val destinationName: String = "Central Station",
    val missedByLabel: String = "2 min ago",
)

class RecoveryViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(RecoveryUiState())
    val state: StateFlow<RecoveryUiState> = mutableState.asStateFlow()
}
