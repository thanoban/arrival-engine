package com.nearwake.feature.places

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaceSearchUiState(
    val query: String = "",
    val results: List<String> = listOf(
        "Central Station",
        "Airport Terminal 2",
        "University Gate",
    ),
)

class PlaceSearchViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(PlaceSearchUiState())
    val state: StateFlow<PlaceSearchUiState> = mutableState.asStateFlow()

    fun updateQuery(value: String) {
        mutableState.value = mutableState.value.copy(query = value)
    }
}
