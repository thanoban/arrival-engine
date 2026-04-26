package com.nearwake.data.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.nearwake.domain.commute.CommutePrediction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DepartureReminderScheduleResult(
    val scheduledCount: Int,
    val skippedPastCount: Int,
    val skippedOtherDayCount: Int,
    val nextReminderLabel: String?,
)

@Singleton
class DepartureReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val planner = DepartureReminderPlanner()

    fun scheduleToday(predictions: List<CommutePrediction>): DepartureReminderScheduleResult {
        val planningResult = planner.planToday(predictions)
        planningResult.reminders.forEach { reminder ->
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMillis,
                REMINDER_WINDOW_MS,
                pendingIntent(reminder),
            )
        }
        return DepartureReminderScheduleResult(
            scheduledCount = planningResult.reminders.size,
            skippedPastCount = planningResult.skippedPastCount,
            skippedOtherDayCount = planningResult.skippedOtherDayCount,
            nextReminderLabel = planningResult.reminders.firstOrNull()?.let { reminder ->
                "${reminder.leaveByLabel} for ${reminder.destinationName}"
            },
        )
    }

    fun cancel(predictionIds: List<String>) {
        predictionIds.forEach { predictionId ->
            alarmManager.cancel(
                PendingIntent.getBroadcast(
                    context,
                    requestCode(predictionId),
                    Intent(context, DepartureReminderReceiver::class.java),
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                ) ?: return@forEach,
            )
        }
    }

    private fun pendingIntent(reminder: DepartureReminderPlan): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode(reminder.predictionId),
            Intent(context, DepartureReminderReceiver::class.java).apply {
                putExtra(NotificationHelper.EXTRA_PREDICTION_ID, reminder.predictionId)
                putExtra(NotificationHelper.EXTRA_DESTINATION_NAME, reminder.destinationName)
                putExtra(NotificationHelper.EXTRA_LEAVE_BY_LABEL, reminder.leaveByLabel)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun requestCode(predictionId: String): Int = "departure:$predictionId".hashCode()

    companion object {
        const val DEPARTURE_NOTIFICATION_ID_BASE = 2_400
        private const val REMINDER_WINDOW_MS = 10 * 60 * 1000L
    }
}
