package com.nearwake.data.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlertReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getStringExtra(NotificationHelper.EXTRA_TRIP_ID) ?: return
        val recovery = intent.getBooleanExtra(AlertOrchestrator.EXTRA_RECOVERY, false)
        notificationHelper.ensureChannels()
        notificationHelper.notify(
            AlertOrchestrator.ALERT_NOTIFICATION_ID,
            notificationHelper.buildAlertNotification(
                tripId = tripId,
                intensity = com.nearwake.domain.trip.model.AlertIntensity.STANDARD,
                recovery = recovery,
            ),
        )
    }
}
