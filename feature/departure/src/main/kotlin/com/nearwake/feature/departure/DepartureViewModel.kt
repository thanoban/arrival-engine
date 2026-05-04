package com.nearwake.feature.departure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.data.alerts.DepartureReminderScheduleResult
import com.nearwake.data.alerts.DepartureReminderScheduler
import com.nearwake.data.patterns.CommutePredictionRepository
import com.nearwake.domain.commute.CommutePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DepartureUiState(
    val predictions: List<DeparturePredictionUiModel> = emptyList(),
    val isRefreshing: Boolean = false,
    val remindersEnabled: Boolean = true,
    val scheduleStatus: String = "Checking reminders.",
    val errorMessage: String? = null,
)

data class DeparturePredictionUiModel(
    val destinationName: String,
    val destinationId: String,
    val leaveByLabel: String,
    val routeLabel: String,
    val tripCount: Int,
)

@HiltViewModel
class DepartureViewModel @Inject constructor(
    private val repository: CommutePredictionRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val departureReminderScheduler: DepartureReminderScheduler,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DepartureUiState())
    val state: StateFlow<DepartureUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.preferences
                .combine(repository.observePredictions()) { preferences, predictions ->
                    preferences.departureRemindersEnabled to predictions
                }
                .collect { (remindersEnabled, predictions) ->
                    val scheduleStatus = if (remindersEnabled) {
                        departureReminderScheduler.scheduleToday(predictions).statusLabel()
                    } else {
                        departureReminderScheduler.cancel(predictions.map { it.id })
                        "Departure reminders are off."
                    }
                    mutableState.value = mutableState.value.copy(
                        predictions = predictions.toUiModels(),
                        remindersEnabled = remindersEnabled,
                        scheduleStatus = scheduleStatus,
                        errorMessage = null,
                    )
                }
        }
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isRefreshing = true)
            runCatching { repository.refreshPredictions() }
                .onFailure { error ->
                    mutableState.value = mutableState.value.copy(
                        errorMessage = error.message ?: "Could not refresh learned departures.",
                    )
                }
            mutableState.value = mutableState.value.copy(isRefreshing = false)
        }
    }

    fun setDepartureRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setDepartureRemindersEnabled(enabled)
        }
    }
}

private fun DepartureReminderScheduleResult.statusLabel(): String =
    when {
        scheduledCount > 0 && nextReminderLabel != null ->
            "Next flexible reminder: $nextReminderLabel."
        skippedPastCount > 0 ->
            "Today's learned leave-by times have already passed."
        else ->
            "No learned departures are scheduled for today."
    }

private fun List<CommutePrediction>.toUiModels(): List<DeparturePredictionUiModel> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val todayDow = now.dayOfWeek.value
    return mapNotNull { prediction ->
        if (todayDow !in prediction.daysOfWeek) return@mapNotNull null
        val h = prediction.typicalDepartureHour
        val m = prediction.typicalDepartureMinute
        val ampm = if (h < 12) "AM" else "PM"
        val displayHour = if (h % 12 == 0) 12 else h % 12
        val minuteStr = m.toString().padStart(2, '0')
        DeparturePredictionUiModel(
            destinationName = prediction.destinationName,
            destinationId = prediction.destinationId,
            leaveByLabel = "$displayHour:$minuteStr $ampm",
            routeLabel = "~${prediction.avgDurationMinutes} min trip",
            tripCount = prediction.tripCount,
        )
    }
}
