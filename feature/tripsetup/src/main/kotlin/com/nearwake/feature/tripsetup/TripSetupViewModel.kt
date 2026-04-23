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
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
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
    val etaLabel: String = "Checking route",
    val routeSummary: String = "NearWake will fall back to destination-only monitoring if a transit route is unavailable.",
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
    private val locationRepository: LocationRepository,
    private val routingRepository: RoutingRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val placeId = savedStateHandle.get<String>(PLACE_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSetupUiState())
    val state: StateFlow<TripSetupUiState> = mutableState.asStateFlow()
    private var previewRouteSnapshot: RouteSnapshot? = null

    init {
        scope.launch {
            val place = savedPlaceDao.getSavedPlaceById(placeId)
            mutableState.value = mutableState.value.copy(
                destinationName = place?.name ?: "Destination unavailable",
                destinationAddress = place?.address.orEmpty(),
                canStart = place != null,
            )
            if (place != null) {
                loadRoutePreview(destination = LatLng(lat = place.lat, lng = place.lng))
            } else {
                mutableState.value = mutableState.value.copy(
                    etaLabel = "Destination unavailable",
                    routeSummary = "Pick another destination to start a trip.",
                )
            }
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
            previewRouteSnapshot?.let { routeSnapshot ->
                routingRepository.cacheRouteForTrip(tripId = tripId, routeSnapshot = routeSnapshot)
            }

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
                    lastEtaMinutes = previewRouteSnapshot?.totalDurationMinutes ?: 35,
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

    private suspend fun loadRoutePreview(destination: LatLng) {
        val origin = runCatching { locationRepository.getLastKnownLocation() }.getOrNull()
        if (origin == null) {
            mutableState.value = mutableState.value.copy(
                etaLabel = "Destination-only",
                routeSummary = "No last known location is available yet, so NearWake will arm destination-only monitoring.",
            )
            return
        }

        routingRepository.fetchRoute(origin = origin, destination = destination)
            .onSuccess { routeSnapshot ->
                previewRouteSnapshot = routeSnapshot
                mutableState.value = mutableState.value.copy(
                    etaLabel = "~${routeSnapshot.totalDurationMinutes} min",
                    routeSummary = buildRouteSummary(routeSnapshot),
                )
            }
            .onFailure {
                mutableState.value = mutableState.value.copy(
                    etaLabel = "Destination-only",
                    routeSummary = "Transit routing is unavailable on this device right now, so NearWake will monitor only the destination.",
                )
            }
    }

    private fun buildRouteSummary(routeSnapshot: RouteSnapshot): String {
        val stopCount = routeSnapshot.stops.size
        val transferCount = routeSnapshot.transfers.size
        val stopLabel = if (stopCount == 1) "1 stop" else "$stopCount stops"
        val transferLabel = if (transferCount == 1) "1 transfer" else "$transferCount transfers"
        return "$stopLabel · $transferLabel"
    }

    companion object {
        const val PLACE_ID_ARG = "placeId"
    }
}
