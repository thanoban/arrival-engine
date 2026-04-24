package com.nearwake.feature.settings

import androidx.lifecycle.ViewModel
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultAlertLeadMinutes: Int = 10,
    val alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val alertMode: AlertMode = AlertMode.ACTIVE,
    val backgroundMonitoringEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            userPreferencesDataStore.preferences.collect { preferences ->
                mutableState.value = SettingsUiState(
                    defaultAlertLeadMinutes = preferences.defaultAlertLeadMinutes,
                    alertIntensity = preferences.defaultAlertIntensity,
                    alertMode = preferences.defaultAlertMode,
                    backgroundMonitoringEnabled = preferences.backgroundMonitoringEnabled,
                    diagnosticsEnabled = preferences.diagnosticsEnabled,
                )
            }
        }
    }

    fun updateLeadMinutes(minutes: Int) {
        scope.launch {
            userPreferencesDataStore.updateDefaultAlertLeadMinutes(minutes)
        }
    }

    fun updateAlertIntensity(intensity: AlertIntensity) {
        scope.launch {
            userPreferencesDataStore.updateDefaultAlertIntensity(intensity)
        }
    }

    fun updateAlertMode(mode: AlertMode) {
        scope.launch {
            userPreferencesDataStore.updateDefaultAlertMode(mode)
        }
    }

    fun setBackgroundMonitoringEnabled(enabled: Boolean) {
        scope.launch {
            userPreferencesDataStore.setBackgroundMonitoringEnabled(enabled)
        }
    }

    fun setDiagnosticsEnabled(enabled: Boolean) {
        scope.launch {
            userPreferencesDataStore.setDiagnosticsEnabled(enabled)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}
