package com.nearwake.feature.alerts

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.data.alerts.TripMonitoringService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.datetime.Clock

data class RecoveryUiState(
    val destinationName: String = "Recovery",
    val missedByLabel: String = "moments ago",
)

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
    private val tripSessionDao: TripSessionDao,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(RecoveryUiState())
    val state: StateFlow<RecoveryUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
                tripSessionDao.observeTripSession(tripId),
            ) { trip, places, session ->
                val place = trip?.destinationId?.let { destinationId -> places.firstOrNull { it.id == destinationId } }
                RecoveryUiState(
                    destinationName = place?.name ?: "Recovery",
                    missedByLabel = session?.updatedAt?.toString()?.replace('T', ' ')?.take(16) ?: "moments ago",
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun endTrip(onEnded: () -> Unit) {
        scope.launch {
            tripDao.getTripById(tripId)?.let { trip ->
                tripDao.upsertTrip(trip.copy(completedAt = Clock.System.now()))
            }
            tripSessionDao.deleteTripSession(tripId)
            TripMonitoringService.stop(appContext)
            onEnded()
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
