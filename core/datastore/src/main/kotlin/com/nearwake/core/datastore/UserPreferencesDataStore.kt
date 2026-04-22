package com.nearwake.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nearwake.core.datastore.model.UserPreferences
import com.nearwake.domain.trip.model.AlertIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            defaultAlertLeadMinutes = preferences[DEFAULT_ALERT_LEAD_MINUTES] ?: 10,
            defaultAlertIntensity = preferences[DEFAULT_ALERT_INTENSITY]
                ?.let(AlertIntensity::valueOf)
                ?: AlertIntensity.STANDARD,
            backgroundMonitoringEnabled = preferences[BACKGROUND_MONITORING_ENABLED] ?: true,
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            diagnosticsEnabled = preferences[DIAGNOSTICS_ENABLED] ?: false,
            notificationChannelVersion = preferences[NOTIFICATION_CHANNEL_VERSION] ?: 1,
        )
    }

    suspend fun updateDefaultAlertLeadMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_LEAD_MINUTES] = minutes
        }
    }

    suspend fun updateDefaultAlertIntensity(intensity: AlertIntensity) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_INTENSITY] = intensity.name
        }
    }

    suspend fun setBackgroundMonitoringEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_MONITORING_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setDiagnosticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DIAGNOSTICS_ENABLED] = enabled
        }
    }

    suspend fun updateNotificationChannelVersion(version: Int) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_CHANNEL_VERSION] = version
        }
    }

    suspend fun updateAll(values: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_LEAD_MINUTES] = values.defaultAlertLeadMinutes
            preferences[DEFAULT_ALERT_INTENSITY] = values.defaultAlertIntensity.name
            preferences[BACKGROUND_MONITORING_ENABLED] = values.backgroundMonitoringEnabled
            preferences[ONBOARDING_COMPLETED] = values.onboardingCompleted
            preferences[DIAGNOSTICS_ENABLED] = values.diagnosticsEnabled
            preferences[NOTIFICATION_CHANNEL_VERSION] = values.notificationChannelVersion
        }
    }

    companion object {
        private val DEFAULT_ALERT_LEAD_MINUTES = intPreferencesKey("default_alert_lead_minutes")
        private val DEFAULT_ALERT_INTENSITY = stringPreferencesKey("default_alert_intensity")
        private val BACKGROUND_MONITORING_ENABLED = booleanPreferencesKey("background_monitoring_enabled")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val DIAGNOSTICS_ENABLED = booleanPreferencesKey("diagnostics_enabled")
        private val NOTIFICATION_CHANNEL_VERSION = intPreferencesKey("notification_channel_version")
    }
}
