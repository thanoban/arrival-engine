package com.nearwake.data.alerts

import com.nearwake.domain.commute.CommutePrediction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class DepartureReminderPlan(
    val predictionId: String,
    val destinationName: String,
    val triggerAtMillis: Long,
    val leaveByLabel: String,
)

data class DepartureReminderPlanningResult(
    val reminders: List<DepartureReminderPlan>,
    val skippedPastCount: Int,
    val skippedOtherDayCount: Int,
)

class DepartureReminderPlanner(
    private val clock: Clock = Clock.System,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    fun planToday(predictions: List<CommutePrediction>): DepartureReminderPlanningResult {
        val now = clock.now()
        val nowLocal = now.toLocalDateTime(timeZone)
        val today = nowLocal.dayOfWeek.value
        var skippedPast = 0
        var skippedOtherDay = 0
        val reminders = predictions.mapNotNull { prediction ->
            if (today !in prediction.daysOfWeek) {
                skippedOtherDay += 1
                return@mapNotNull null
            }

            val triggerAt = prediction.triggerAt(nowLocal, timeZone)
            if (triggerAt <= now) {
                skippedPast += 1
                return@mapNotNull null
            }

            DepartureReminderPlan(
                predictionId = prediction.id,
                destinationName = prediction.destinationName,
                triggerAtMillis = triggerAt.toEpochMilliseconds(),
                leaveByLabel = prediction.leaveByLabel(),
            )
        }.sortedBy { it.triggerAtMillis }

        return DepartureReminderPlanningResult(
            reminders = reminders,
            skippedPastCount = skippedPast,
            skippedOtherDayCount = skippedOtherDay,
        )
    }
}

private fun CommutePrediction.triggerAt(
    nowLocal: LocalDateTime,
    timeZone: TimeZone,
): Instant =
    LocalDateTime(
        date = nowLocal.date,
        time = LocalTime(
            hour = typicalDepartureHour.coerceIn(0, 23),
            minute = typicalDepartureMinute.coerceIn(0, 59),
        ),
    ).toInstant(timeZone)

private fun CommutePrediction.leaveByLabel(): String {
    val hour = typicalDepartureHour.coerceIn(0, 23)
    val minute = typicalDepartureMinute.coerceIn(0, 59)
    val ampm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour % 12 == 0) 12 else hour % 12
    return "$displayHour:${minute.toString().padStart(2, '0')} $ampm"
}
