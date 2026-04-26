package com.nearwake.feature.departure

import androidx.lifecycle.ViewModel
import com.nearwake.data.patterns.CommutePredictionRepository
import com.nearwake.domain.commute.CommutePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class DepartureUiState(
    val predictions: List<DeparturePredictionUiModel> = emptyList(),
    val isRefreshing: Boolean = false,
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
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(DepartureUiState())
    val state: StateFlow<DepartureUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            repository.observePredictions().collect { predictions ->
                mutableState.value = mutableState.value.copy(
                    predictions = predictions.toUiModels(),
                )
            }
        }
        scope.launch {
            mutableState.value = mutableState.value.copy(isRefreshing = true)
            runCatching { repository.refreshPredictions() }
            mutableState.value = mutableState.value.copy(isRefreshing = false)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
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
