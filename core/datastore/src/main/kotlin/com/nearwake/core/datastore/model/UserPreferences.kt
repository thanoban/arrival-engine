package com.nearwake.core.datastore.model

import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class UserPreferences(
    val defaultAlertLeadMinutes: Int = 10,
    val defaultAlertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val defaultAlertMode: AlertMode = AlertMode.ACTIVE,
    val backgroundMonitoringEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val diagnosticsEnabled: Boolean = false,
    val notificationChannelVersion: Int = 1,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val departureRemindersEnabled: Boolean = true,
)
