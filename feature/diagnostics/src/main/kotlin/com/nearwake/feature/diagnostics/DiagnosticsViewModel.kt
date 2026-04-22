package com.nearwake.feature.diagnostics

import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.dao.TripSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val stateLabel: String = "No active trip",
    val registeredGeofences: List<String> = emptyList(),
    val recentEvents: List<String> = emptyList(),
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    tripSessionDao: TripSessionDao,
    diagnosticsEventDao: DiagnosticsEventDao,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(DiagnosticsUiState())
    val state: StateFlow<DiagnosticsUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripSessionDao.observeTripSessions(),
                diagnosticsEventDao.observeRecentEvents(limit = 12),
            ) { sessions, recentEvents ->
                val session = sessions.firstOrNull()
                DiagnosticsUiState(
                    stateLabel = session?.state?.name ?: "No active trip",
                    registeredGeofences = session?.geofenceIds.orEmpty(),
                    recentEvents = recentEvents.map { event ->
                        buildString {
                            append(event.eventType)
                            event.tripId?.let { tripId -> append(" · ").append(tripId.take(8)) }
                        }
                    },
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}
