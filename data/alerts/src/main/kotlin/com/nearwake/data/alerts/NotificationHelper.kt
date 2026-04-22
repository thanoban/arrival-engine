package com.nearwake.data.alerts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.MonitoringMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun ensureChannels() {
        notificationManager.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_MONITORING,
                    "NearWake Monitoring",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Foreground monitoring notifications"
                },
                NotificationChannel(
                    CHANNEL_ALERT,
                    "NearWake Alerts",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Arrival alerts"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
                NotificationChannel(
                    CHANNEL_RECOVERY,
                    "NearWake Recovery",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Recovery notifications when a trip may have been missed"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
            ),
        )
    }

    fun buildMonitoringNotification(mode: MonitoringMode): Notification =
        NotificationCompat.Builder(context, CHANNEL_MONITORING)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("NearWake active")
            .setContentText("Monitoring | ${mode.label}")
            .setOngoing(true)
            .setContentIntent(contentIntent())
            .build()

    fun buildAlertNotification(
        tripId: String,
        intensity: AlertIntensity,
        recovery: Boolean = false,
    ): Notification {
        val channelId = if (recovery) CHANNEL_RECOVERY else CHANNEL_ALERT
        val title = if (recovery) "NearWake recovery alert" else "NearWake arrival alert"
        val body = if (recovery) {
            "We think you may have passed your stop."
        } else {
            "You're near your destination. Alert intensity: ${intensity.name.lowercase()}."
        }
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(contentIntent(tripId), true)
            .setContentIntent(contentIntent(tripId))
            .build()
    }

    fun notify(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    private fun contentIntent(tripId: String? = null): PendingIntent {
        val intent = (context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()).apply {
            tripId?.let { putExtra(EXTRA_TRIP_ID, it) }
        }
        return PendingIntent.getActivity(
            context,
            tripId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private val MonitoringMode.label: String
        get() = when (this) {
            MonitoringMode.GEOFENCE_ONLY -> "Geofence only"
            MonitoringMode.BALANCED -> "Low power"
            MonitoringMode.PRECISE_BURST -> "Approaching"
        }

    companion object {
        const val CHANNEL_MONITORING = "nearwake_monitoring"
        const val CHANNEL_ALERT = "nearwake_alert"
        const val CHANNEL_RECOVERY = "nearwake_recovery"
        const val EXTRA_TRIP_ID = "extra_trip_id"
    }
}
