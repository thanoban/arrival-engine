package com.nearwake.data.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlertReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getStringExtra(NotificationHelper.EXTRA_TRIP_ID) ?: return
        val expiresAt = intent.getLongExtra(AlertOrchestrator.EXTRA_EXPIRES_AT, 0L)
        if (expiresAt <= SystemClock.elapsedRealtime()) return
        val recovery = intent.getBooleanExtra(AlertOrchestrator.EXTRA_RECOVERY, false)
        val mode = intent.getStringExtra(AlertOrchestrator.EXTRA_MODE)
            ?.let { name -> AlertMode.entries.firstOrNull { it.name == name } }
            ?: AlertMode.ACTIVE
        notificationHelper.ensureChannels()
        notificationHelper.notify(
            AlertOrchestrator.ALERT_NOTIFICATION_ID,
            notificationHelper.buildAlertNotification(
                tripId = tripId,
                intensity = AlertIntensity.STANDARD,
                mode = mode,
                recovery = recovery,
            ),
        )
    }
}
