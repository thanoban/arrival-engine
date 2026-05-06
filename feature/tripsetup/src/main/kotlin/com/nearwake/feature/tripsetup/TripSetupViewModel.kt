package com.nearwake.feature.tripsetup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.LoadTripSetupPreviewUseCase
import com.nearwake.application.trip.StartTripRequest
import com.nearwake.application.trip.StartTripUseCase
import com.nearwake.core.datastore.UserPreferencesDataStore
import com.nearwake.domain.routing.model.RouteSnapshot
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
    val errorMessage: String? = null,
)

@HiltViewModel
class TripSetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadTripSetupPreview: LoadTripSetupPreviewUseCase,
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
            val preview = loadTripSetupPreview(placeId)
            previewRouteSnapshot = preview.previewRouteSnapshot
            mutableState.value = mutableState.value.copy(
                destinationName = preview.destinationName,
                destinationAddress = preview.destinationAddress,
                etaLabel = preview.etaLabel,
                routeSummary = preview.routeSummary,
                canStart = preview.canStart,
            )
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

    companion object {
        const val PLACE_ID_ARG = "placeId"
    }
}
