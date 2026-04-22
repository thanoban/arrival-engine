package com.nearwake.feature.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnboardingUiState(
    val pages: List<String> = listOf(
        "Set a destination fast",
        "Monitor in the background",
        "Wake up before you miss the stop",
    ),
)

class OnboardingViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()
}
