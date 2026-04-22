package com.nearwake.data.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlertReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getStringExtra(NotificationHelper.EXTRA_TRIP_ID) ?: return
        val recovery = intent.getBooleanExtra(AlertOrchestrator.EXTRA_RECOVERY, false)
        val helper = NotificationHelper(context)
        helper.ensureChannels()
        helper.notify(
            AlertOrchestrator.ALERT_NOTIFICATION_ID,
            helper.buildAlertNotification(
                tripId = tripId,
                intensity = com.nearwake.domain.trip.model.AlertIntensity.STANDARD,
                recovery = recovery,
            ),
        )
    }
}
