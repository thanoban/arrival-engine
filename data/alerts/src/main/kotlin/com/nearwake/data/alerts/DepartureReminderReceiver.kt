package com.nearwake.data.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DepartureReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val predictionId = intent.getStringExtra(NotificationHelper.EXTRA_PREDICTION_ID) ?: return
        val destinationName = intent.getStringExtra(NotificationHelper.EXTRA_DESTINATION_NAME) ?: return
        val leaveByLabel = intent.getStringExtra(NotificationHelper.EXTRA_LEAVE_BY_LABEL) ?: return

        notificationHelper.ensureChannels()
        notificationHelper.notify(
            DepartureReminderScheduler.DEPARTURE_NOTIFICATION_ID_BASE + (predictionId.hashCode() and 0x7fffffff) % 500,
            notificationHelper.buildDepartureReminderNotification(
                destinationName = destinationName,
                leaveByLabel = leaveByLabel,
            ),
        )
    }
}
