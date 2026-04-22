package com.nearwake.feature.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HistoryUiState(
    val trips: List<String> = listOf(
        "Central Station · Completed 2h ago",
        "Airport Terminal 2 · Yesterday",
    ),
)

class HistoryViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = mutableState.asStateFlow()
}
