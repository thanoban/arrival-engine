package com.nearwake.feature.tripsetup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.monitoring.StartTripMonitoringUseCase
import com.nearwake.application.trip.StartTripRequest
import com.nearwake.application.trip.StartTripUseCase
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.repository.LocationRepository
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.AlertIntensity
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.TripRule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
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
    val alertTriggerMode: AlertTriggerMode = AlertTriggerMode.TIME,
    val alertDistanceMeters: Int = TripRule.DEFAULT_ALERT_DISTANCE_METERS,
    val alertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val alertMode: AlertMode = AlertMode.ACTIVE,
    val backgroundMonitoringEnabled: Boolean = true,
    val canStart: Boolean = false,
)

@HiltViewModel
class TripSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedPlaceDao: SavedPlaceDao,
    private val locationRepository: LocationRepository,
    private val routingRepository: RoutingRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val startTrip: StartTripUseCase,
) : ViewModel() {
    private val placeId = savedStateHandle.get<String>(PLACE_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(TripSetupUiState())
    val state: StateFlow<TripSetupUiState> = mutableState.asStateFlow()
    private var previewRouteSnapshot: RouteSnapshot? = null

    init {
        viewModelScope.launch {
            val preferences = userPreferencesDataStore.preferences.first()
            mutableState.value = mutableState.value.copy(
                alertLeadMinutes = preferences.defaultAlertLeadMinutes,
                alertTriggerMode = preferences.defaultAlertTriggerMode,
                alertDistanceMeters = preferences.defaultAlertDistanceMeters,
                alertIntensity = preferences.defaultAlertIntensity,
                alertMode = preferences.defaultAlertMode,
                backgroundMonitoringEnabled = preferences.backgroundMonitoringEnabled,
            )
        }
        viewModelScope.launch {
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

    fun selectAlertTriggerMode(triggerMode: AlertTriggerMode) {
        mutableState.value = mutableState.value.copy(alertTriggerMode = triggerMode)
    }

    fun selectAlertDistanceMeters(distanceMeters: Int) {
        mutableState.value = mutableState.value.copy(alertDistanceMeters = distanceMeters)
    }

    fun selectIntensity(intensity: AlertIntensity) {
        mutableState.value = mutableState.value.copy(alertIntensity = intensity)
    }

    fun selectAlertMode(mode: AlertMode) {
        mutableState.value = mutableState.value.copy(alertMode = mode)
    }

    fun setBackgroundMonitoringEnabled(enabled: Boolean) {
        mutableState.value = mutableState.value.copy(backgroundMonitoringEnabled = enabled)
    }

    fun startTrip(onStarted: (String) -> Unit) {
        viewModelScope.launch {
            val uiState = mutableState.value
            startTrip(
                StartTripRequest(
                    placeId = placeId,
                    alertLeadMinutes = uiState.alertLeadMinutes,
                    alertTriggerMode = uiState.alertTriggerMode,
                    alertDistanceMeters = uiState.alertDistanceMeters,
                    alertIntensity = uiState.alertIntensity,
                    alertMode = uiState.alertMode,
                    previewRouteSnapshot = previewRouteSnapshot,
                ),
            )?.let(onStarted)
        }
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
