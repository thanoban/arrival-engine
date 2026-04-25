package com.nearwake.data.alerts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
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
                NotificationChannel(
                    CHANNEL_APPROACH,
                    "NearWake Approach",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Gentle approach warning — stop is a few minutes away"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    enableVibration(false)
                },
                NotificationChannel(
                    CHANNEL_IMMINENT,
                    "NearWake Imminent",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Strong imminent warning — get ready to exit"
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    enableVibration(true)
                    vibrationPattern = IMMINENT_VIBRATION_PATTERN
                },
            ),
        )
    }

    fun buildMonitoringNotification(
        mode: MonitoringMode,
        stage: AlertStage = AlertStage.MONITORING,
        destinationName: String? = null,
        etaMinutes: Int? = null,
        confidence: Confidence? = null,
        alertMode: AlertMode? = null,
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_MONITORING)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle(destinationName?.let { "NearWake guarding $it" } ?: "NearWake active")
            .setContentText(
                buildList {
                    add(stage.monitoringLabel)
                    etaMinutes?.let { add("~${it} min") }
                    confidence?.let { add(it.label) }
                }.joinToString(" | "),
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    buildList {
                        destinationName?.let { add("Destination: $it") }
                        add("Stage: ${stage.monitoringLabel}")
                        add("Monitoring: ${mode.label}")
                        etaMinutes?.let { add("ETA: about $it minutes") }
                        confidence?.let { add("Confidence: ${it.label}") }
                        alertMode?.let { add("Mode: ${it.label}") }
                    }.joinToString("  •  "),
                ),
            )
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(contentIntent())
            .build()

    fun buildStageNotification(
        tripId: String,
        destinationName: String,
        stage: AlertStage,
        mode: AlertMode,
    ): Notification {
        val channelId = if (stage == AlertStage.IMMINENT) CHANNEL_IMMINENT else CHANNEL_APPROACH
        val priority = if (stage == AlertStage.IMMINENT) {
            NotificationCompat.PRIORITY_HIGH
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(stage.notificationTitle(destinationName))
            .setContentText(stage.notificationBody(mode))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(tripId))
        if (stage == AlertStage.IMMINENT) {
            builder.setVibrate(IMMINENT_VIBRATION_PATTERN)
        }
        return builder.build()
    }

    fun buildTransferNotification(
        tripId: String,
        stopName: String,
        lineName: String,
        remainingMinutes: Int,
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_APPROACH)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Transfer coming up")
            .setContentText(
                if (remainingMinutes <= 1) {
                    "Change to $lineName at $stopName now."
                } else {
                    "Change to $lineName at $stopName in $remainingMinutes min."
                },
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(tripId))
            .build()

    fun buildBoardingWarningNotification(
        tripId: String,
        destinationName: String,
    ): Notification =
        NotificationCompat.Builder(context, CHANNEL_IMMINENT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Check the route direction")
            .setContentText("This vehicle may be heading away from $destinationName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setVibrate(IMMINENT_VIBRATION_PATTERN)
            .setContentIntent(contentIntent(tripId))
            .build()

    fun buildAlertNotification(
        tripId: String,
        intensity: AlertIntensity,
        mode: AlertMode,
        recovery: Boolean = false,
    ): Notification {
        val channelId = if (recovery) CHANNEL_RECOVERY else CHANNEL_ALERT
        val title = if (recovery) "NearWake recovery alert" else "NearWake arrival alert"
        val body = if (recovery) {
            "We think you may have passed your stop."
        } else {
            "You're near your destination. ${mode.label} mode with ${intensity.name.lowercase()} intensity."
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
        const val CHANNEL_APPROACH = "nearwake_approach"
        const val CHANNEL_IMMINENT = "nearwake_imminent"
        const val EXTRA_TRIP_ID = "extra_trip_id"
        val IMMINENT_VIBRATION_PATTERN = longArrayOf(0L, 200L, 100L, 400L)
    }
}

private val AlertMode.label: String
    get() = when (this) {
        AlertMode.ACTIVE -> "Active"
        AlertMode.SLEEP -> "Sleep"
    }

private val AlertStage.monitoringLabel: String
    get() = when (this) {
        AlertStage.MONITORING -> "Monitoring"
        AlertStage.APPROACH -> "Approach stage"
        AlertStage.IMMINENT -> "Imminent stage"
        AlertStage.ARRIVAL -> "Arrival stage"
        AlertStage.RECOVERY -> "Recovery stage"
    }

private val Confidence.label: String
    get() = when (this) {
        Confidence.HIGH -> "High confidence"
        Confidence.DEGRADED -> "Medium confidence"
        Confidence.OFFLINE -> "Low confidence"
    }

private fun AlertStage.notificationTitle(destinationName: String): String = when (this) {
    AlertStage.APPROACH -> "Approaching $destinationName"
    AlertStage.IMMINENT -> "Get ready for $destinationName"
    AlertStage.ARRIVAL -> "Arriving at $destinationName"
    AlertStage.RECOVERY -> "Recovery for $destinationName"
    AlertStage.MONITORING -> "NearWake update"
}

private fun AlertStage.notificationBody(mode: AlertMode): String = when (this) {
    AlertStage.APPROACH -> "NearWake has entered the approach window. ${mode.label} mode is standing by."
    AlertStage.IMMINENT -> "Your stop is close. ${mode.label} mode is preparing a stronger alert."
    AlertStage.ARRIVAL -> "It is time to get off now."
    AlertStage.RECOVERY -> "NearWake thinks you may have missed the stop."
    AlertStage.MONITORING -> "NearWake is monitoring this trip."
}
