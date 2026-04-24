package com.nearwake.data.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.nearwake.core.database.dao.AlertEventDao
import com.nearwake.core.database.entity.AlertEventEntity
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class AlertOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHelper: NotificationHelper,
    private val alertEventDao: AlertEventDao,
    private val diagnosticsLogger: DiagnosticsLogger,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var mediaPlayer: MediaPlayer? = null
    private var activeAlertEventId: String? = null

    suspend fun fireAlert(
        tripId: String,
        intensity: AlertIntensity,
        mode: AlertMode,
        type: AlertType = AlertType.ARRIVAL,
    ) {
        notificationHelper.ensureChannels()
        val eventId = UUID.randomUUID().toString()
        activeAlertEventId = eventId

        alertEventDao.upsertAlertEvent(
            AlertEventEntity(
                id = eventId,
                tripId = tripId,
                firedAt = Clock.System.now(),
                type = type,
            ),
        )

        notificationHelper.notify(
            ALERT_NOTIFICATION_ID,
            notificationHelper.buildAlertNotification(
                tripId = tripId,
                intensity = intensity,
                mode = mode,
                recovery = type == AlertType.RECOVERY,
            ),
        )
        startSound()
        vibrate(
            intensity = intensity,
            mode = mode,
        )
        scheduleRepeatingReminder(
            tripId = tripId,
            mode = mode,
            recovery = type == AlertType.RECOVERY,
        )

        diagnosticsLogger.log(
            eventType = "alert_fired",
            tripId = tripId,
            payload = buildJsonObject {
                put("type", type.name)
                put("intensity", intensity.name)
                put("mode", mode.name)
            },
        )
    }

    suspend fun dismissAlert(tripId: String) {
        activeAlertEventId?.let { eventId ->
            alertEventDao.getAlertEventById(eventId)?.let { existing ->
                alertEventDao.upsertAlertEvent(
                    existing.copy(dismissedAt = Clock.System.now()),
                )
            }
        }
        activeAlertEventId = null
        alarmManager.cancel(reminderPendingIntent(tripId))
        vibrator.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        notificationHelper.cancel(ALERT_NOTIFICATION_ID)

        diagnosticsLogger.log(
            eventType = "alert_dismissed",
            tripId = tripId,
            payload = buildJsonObject {},
        )
    }

    private fun startSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            setDataSource(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            isLooping = true
            prepare()
            start()
        }
    }

    private fun vibrate(
        intensity: AlertIntensity,
        mode: AlertMode,
    ) {
        val pattern = when (intensity) {
            AlertIntensity.GENTLE -> if (mode == AlertMode.SLEEP) {
                longArrayOf(0L, 250L, 150L, 350L, 150L, 450L)
            } else {
                longArrayOf(0L, 250L, 200L, 250L)
            }

            AlertIntensity.STANDARD -> if (mode == AlertMode.SLEEP) {
                longArrayOf(0L, 300L, 150L, 450L, 150L, 600L)
            } else {
                longArrayOf(0L, 400L, 200L, 400L, 200L, 400L)
            }

            AlertIntensity.LOUD -> if (mode == AlertMode.SLEEP) {
                longArrayOf(0L, 800L, 120L, 800L, 120L, 950L)
            } else {
                longArrayOf(0L, 700L, 150L, 700L, 150L, 700L)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun scheduleRepeatingReminder(
        tripId: String,
        mode: AlertMode,
        recovery: Boolean,
    ) {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + REMINDER_INTERVAL_MS,
            REMINDER_INTERVAL_MS,
            reminderPendingIntent(
                tripId = tripId,
                mode = mode,
                recovery = recovery,
            ),
        )
    }

    private fun reminderPendingIntent(
        tripId: String,
        mode: AlertMode = AlertMode.ACTIVE,
        recovery: Boolean = false,
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            tripId.hashCode(),
            Intent(context, AlertReminderReceiver::class.java).apply {
                putExtra(NotificationHelper.EXTRA_TRIP_ID, tripId)
                putExtra(EXTRA_RECOVERY, recovery)
                putExtra(EXTRA_MODE, mode.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    companion object {
        const val ALERT_NOTIFICATION_ID = 42
        const val EXTRA_RECOVERY = "extra_recovery"
        const val EXTRA_MODE = "extra_mode"
        private const val REMINDER_INTERVAL_MS = 30_000L
    }
}
