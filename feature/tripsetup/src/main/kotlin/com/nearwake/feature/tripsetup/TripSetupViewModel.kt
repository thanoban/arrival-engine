package com.nearwake.feature.tripsetup

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.data.alerts.TripMonitoringService
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
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

data class TripSetupUiState(
    val destinationName: String = "Loading destination",
    val destinationAddress: String = "",
    val etaLabel: String = "~35 min",
    val alertLeadMinutes: Int = 10,
    val alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val backgroundMonitoringEnabled: Boolean = true,
    val canStart: Boolean = false,
)

@HiltViewModel
class TripSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedPlaceDao: SavedPlaceDao,
    private val tripDao: TripDao,
    private val tripSessionDao: TripSessionDao,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val placeId = savedStateHandle.get<String>(PLACE_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSetupUiState())
    val state: StateFlow<TripSetupUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            val place = savedPlaceDao.getSavedPlaceById(placeId)
            mutableState.value = mutableState.value.copy(
                destinationName = place?.name ?: "Destination unavailable",
                destinationAddress = place?.address.orEmpty(),
                canStart = place != null,
            )
        }
    }

    fun selectLeadMinutes(minutes: Int) {
        mutableState.value = mutableState.value.copy(alertLeadMinutes = minutes)
    }

    fun selectIntensity(intensity: AlertIntensity) {
        mutableState.value = mutableState.value.copy(alertIntensity = intensity)
    }

    fun setBackgroundMonitoringEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(backgroundMonitoringEnabled = enabled)
    }

    fun startTrip(onStarted: (String) -> Unit) {
        scope.launch {
            val place = savedPlaceDao.getSavedPlaceById(placeId) ?: return@launch
            val tripId = UUID.randomUUID().toString()
            val now = Clock.System.now()
            val uiState = mutableState.value

            savedPlaceDao.upsertSavedPlace(place.copy(lastUsedAt = now))
            tripDao.upsertTrip(
                TripEntity(
                    id = tripId,
                    destinationId = place.id,
                    alertLeadMinutes = uiState.alertLeadMinutes,
                    alertIntensity = uiState.alertIntensity,
                    createdAt = now,
                ),
            )
            tripSessionDao.upsertTripSession(
                TripSessionEntity(
                    tripId = tripId,
                    state = TripState.Armed,
                    monitoringMode = MonitoringMode.GEOFENCE_ONLY,
                    confidence = Confidence.HIGH,
                    geofenceIds = emptyList(),
                    lastEtaMinutes = 35,
                    updatedAt = now,
                ),
            )
            TripMonitoringService.start(appContext, tripId)
            onStarted(tripId)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    companion object {
        const val PLACE_ID_ARG = "placeId"
    }
}
