package com.nearwake.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.core.datastore.model.UserPreferences
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.TripRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            defaultAlertLeadMinutes = preferences[DEFAULT_ALERT_LEAD_MINUTES] ?: 10,
            defaultAlertTriggerMode = preferences[DEFAULT_ALERT_TRIGGER_MODE]
                ?.let(AlertTriggerMode::valueOf)
                ?: AlertTriggerMode.TIME,
            defaultAlertDistanceMeters = preferences[DEFAULT_ALERT_DISTANCE_METERS]
                ?: TripRule.DEFAULT_ALERT_DISTANCE_METERS,
            defaultAlertIntensity = preferences[DEFAULT_ALERT_INTENSITY]
                ?.let(AlertIntensity::valueOf)
                ?: AlertIntensity.STANDARD,
            defaultAlertMode = preferences[DEFAULT_ALERT_MODE]
                ?.let(AlertMode::valueOf)
                ?: AlertMode.ACTIVE,
            backgroundMonitoringEnabled = preferences[BACKGROUND_MONITORING_ENABLED] ?: true,
            onboardingCompleted = preferences[ONBOARDING_COMPLETED] ?: false,
            diagnosticsEnabled = preferences[DIAGNOSTICS_ENABLED] ?: false,
            notificationChannelVersion = preferences[NOTIFICATION_CHANNEL_VERSION] ?: 1,
            themeMode = preferences[THEME_MODE]?.toEnumOrDefault(ThemeMode.SYSTEM) ?: ThemeMode.SYSTEM,
            departureRemindersEnabled = preferences[DEPARTURE_REMINDERS_ENABLED] ?: true,
        )
    }

    suspend fun updateDefaultAlertLeadMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_LEAD_MINUTES] = minutes
        }
    }

    suspend fun updateDefaultAlertTriggerMode(triggerMode: AlertTriggerMode) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_TRIGGER_MODE] = triggerMode.name
        }
    }

    suspend fun updateDefaultAlertDistanceMeters(distanceMeters: Int) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_DISTANCE_METERS] = distanceMeters
        }
    }

    suspend fun updateDefaultAlertIntensity(intensity: AlertIntensity) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_INTENSITY] = intensity.name
        }
    }

    suspend fun updateDefaultAlertMode(mode: AlertMode) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_MODE] = mode.name
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

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode.name
        }
    }

    suspend fun setDepartureRemindersEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEPARTURE_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateAll(values: UserPreferences) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ALERT_LEAD_MINUTES] = values.defaultAlertLeadMinutes
            preferences[DEFAULT_ALERT_TRIGGER_MODE] = values.defaultAlertTriggerMode.name
            preferences[DEFAULT_ALERT_DISTANCE_METERS] = values.defaultAlertDistanceMeters
            preferences[DEFAULT_ALERT_INTENSITY] = values.defaultAlertIntensity.name
            preferences[DEFAULT_ALERT_MODE] = values.defaultAlertMode.name
            preferences[BACKGROUND_MONITORING_ENABLED] = values.backgroundMonitoringEnabled
            preferences[ONBOARDING_COMPLETED] = values.onboardingCompleted
            preferences[DIAGNOSTICS_ENABLED] = values.diagnosticsEnabled
            preferences[NOTIFICATION_CHANNEL_VERSION] = values.notificationChannelVersion
            preferences[THEME_MODE] = values.themeMode.name
            preferences[DEPARTURE_REMINDERS_ENABLED] = values.departureRemindersEnabled
        }
    }

    companion object {
        private val DEFAULT_ALERT_LEAD_MINUTES = intPreferencesKey("default_alert_lead_minutes")
        private val DEFAULT_ALERT_TRIGGER_MODE = stringPreferencesKey("default_alert_trigger_mode")
        private val DEFAULT_ALERT_DISTANCE_METERS = intPreferencesKey("default_alert_distance_meters")
        private val DEFAULT_ALERT_INTENSITY = stringPreferencesKey("default_alert_intensity")
        private val DEFAULT_ALERT_MODE = stringPreferencesKey("default_alert_mode")
        private val BACKGROUND_MONITORING_ENABLED = booleanPreferencesKey("background_monitoring_enabled")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val DIAGNOSTICS_ENABLED = booleanPreferencesKey("diagnostics_enabled")
        private val NOTIFICATION_CHANNEL_VERSION = intPreferencesKey("notification_channel_version")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DEPARTURE_REMINDERS_ENABLED = booleanPreferencesKey("departure_reminders_enabled")
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)
