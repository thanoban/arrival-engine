package com.nearwake.core.datastore.model

import com.nearwake.domain.trip.model.AlertIntensity

data class UserPreferences(
    val defaultAlertLeadMinutes: Int = 10,
    val defaultAlertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val backgroundMonitoringEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val diagnosticsEnabled: Boolean = false,
    val notificationChannelVersion: Int = 1,
)
