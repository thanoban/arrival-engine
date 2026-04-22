package com.nearwake.feature.permissions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionsUiState(
    val notificationGranted: Boolean = false,
    val fineLocationGranted: Boolean = false,
    val backgroundLocationGranted: Boolean = false,
    val activityRecognitionGranted: Boolean = false,
)

class PermissionsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(PermissionsUiState())
    val state: StateFlow<PermissionsUiState> = mutableState.asStateFlow()
}
