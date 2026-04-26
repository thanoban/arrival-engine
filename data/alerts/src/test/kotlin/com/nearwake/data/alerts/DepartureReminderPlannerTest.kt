package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.commute.CommutePrediction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test

class DepartureReminderPlannerTest {
    @Test
    fun `planToday schedules future prediction for today`() {
        val planner = DepartureReminderPlanner(
            clock = fixedClock("2026-04-27T07:15:00Z"),
            timeZone = TimeZone.UTC,
        )

        val result = planner.planToday(
            listOf(prediction(hour = 8, minute = 30, daysOfWeek = setOf(1))),
        )

        assertThat(result.reminders).hasSize(1)
        assertThat(result.reminders.first().leaveByLabel).isEqualTo("8:30 AM")
        assertThat(result.skippedPastCount).isEqualTo(0)
        assertThat(result.skippedOtherDayCount).isEqualTo(0)
    }

    @Test
    fun `planToday skips predictions for past times and other days`() {
        val planner = DepartureReminderPlanner(
            clock = fixedClock("2026-04-27T09:00:00Z"),
            timeZone = TimeZone.UTC,
        )

        val result = planner.planToday(
            listOf(
                prediction(id = "past", hour = 8, minute = 30, daysOfWeek = setOf(1)),
                prediction(id = "other-day", hour = 10, minute = 0, daysOfWeek = setOf(2)),
            ),
        )

        assertThat(result.reminders).isEmpty()
        assertThat(result.skippedPastCount).isEqualTo(1)
        assertThat(result.skippedOtherDayCount).isEqualTo(1)
    }

    private fun fixedClock(isoInstant: String): Clock =
        object : Clock {
            override fun now(): Instant = Instant.parse(isoInstant)
        }

    private fun prediction(
        id: String = "prediction-1",
        hour: Int,
        minute: Int,
        daysOfWeek: Set<Int>,
    ): CommutePrediction =
        CommutePrediction(
            id = id,
            originId = null,
            destinationId = "dest-$id",
            destinationName = "Central Station",
            daysOfWeek = daysOfWeek,
            typicalDepartureHour = hour,
            typicalDepartureMinute = minute,
            avgDurationMinutes = 25,
            tripCount = 4,
        )
}
