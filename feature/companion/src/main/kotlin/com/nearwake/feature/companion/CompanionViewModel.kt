package com.nearwake.feature.companion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.ObserveCompanionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompanionUiState(
    val tripId: String = "",
    val title: String = "Arrival confirmation",
    val messagePreview: String = "I arrived safely.",
    val smsPreview: String = "I arrived safely.",
    val errorMessage: String? = null,
)

@HiltViewModel
class CompanionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCompanion: ObserveCompanionUseCase,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(CompanionUiState(tripId = tripId))
    val state: StateFlow<CompanionUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCompanion(tripId).collect { presentation ->
                mutableState.value = CompanionUiState(
                    tripId = presentation.tripId,
                    title = presentation.title,
                    messagePreview = presentation.messagePreview,
                    smsPreview = presentation.smsPreview,
                )
            }
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
