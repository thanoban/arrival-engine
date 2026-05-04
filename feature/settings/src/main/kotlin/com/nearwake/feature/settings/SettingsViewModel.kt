package com.nearwake.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.TripRule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultAlertLeadMinutes: Int = 10,
    val defaultAlertTriggerMode: AlertTriggerMode = AlertTriggerMode.TIME,
    val defaultAlertDistanceMeters: Int = TripRule.DEFAULT_ALERT_DISTANCE_METERS,
    val alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val alertMode: AlertMode = AlertMode.ACTIVE,
    val backgroundMonitoringEnabled: Boolean = true,
    val diagnosticsEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.preferences.collect { preferences ->
                mutableState.value = SettingsUiState(
                    defaultAlertLeadMinutes = preferences.defaultAlertLeadMinutes,
                    defaultAlertTriggerMode = preferences.defaultAlertTriggerMode,
                    defaultAlertDistanceMeters = preferences.defaultAlertDistanceMeters,
                    alertIntensity = preferences.defaultAlertIntensity,
                    alertMode = preferences.defaultAlertMode,
                    backgroundMonitoringEnabled = preferences.backgroundMonitoringEnabled,
                    diagnosticsEnabled = preferences.diagnosticsEnabled,
                    themeMode = preferences.themeMode,
                )
            }
        }
    }

    fun updateLeadMinutes(minutes: Int) {
        viewModelScope.launch {
            userPreferencesDataStore.updateDefaultAlertLeadMinutes(minutes)
        }
    }

    fun updateAlertTriggerMode(triggerMode: AlertTriggerMode) {
        viewModelScope.launch {
            userPreferencesDataStore.updateDefaultAlertTriggerMode(triggerMode)
        }
    }

    fun updateAlertDistanceMeters(distanceMeters: Int) {
        viewModelScope.launch {
            userPreferencesDataStore.updateDefaultAlertDistanceMeters(distanceMeters)
        }
    }

    fun updateAlertIntensity(intensity: AlertIntensity) {
        viewModelScope.launch {
            userPreferencesDataStore.updateDefaultAlertIntensity(intensity)
        }
    }

    fun updateAlertMode(mode: AlertMode) {
        viewModelScope.launch {
            userPreferencesDataStore.updateDefaultAlertMode(mode)
        }
    }

    fun setBackgroundMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setBackgroundMonitoringEnabled(enabled)
        }
    }

    fun setDiagnosticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setDiagnosticsEnabled(enabled)
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesDataStore.updateThemeMode(themeMode)
        }
    }
}
