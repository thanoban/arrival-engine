package com.nearwake.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.HomeDashboardTone
import com.nearwake.application.trip.ObserveHomeDashboardUseCase
import com.nearwake.application.trip.RearmTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val headline: String = "Travel calmer on the rides that are easiest to miss.",
    val statusLabel: String = "Awaiting trip",
    val statusTone: HomeStatusTone = HomeStatusTone.Neutral,
    val activeTrip: HomeActiveTrip? = null,
    val rearmTrip: HomeRearmTrip? = null,
    val recentTrips: List<HomeRecentTrip> = emptyList(),
    val errorMessage: String? = null,
)

data class HomeActiveTrip(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
)

data class HomeRearmTrip(
    val sourceTripId: String,
    val destinationName: String,
    val subtitle: String,
)

data class HomeRecentTrip(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
    val statusLabel: String,
    val statusTone: HomeStatusTone,
)

enum class HomeStatusTone {
    Safe,
    Monitoring,
    Approaching,
    Neutral,
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeHomeDashboard: ObserveHomeDashboardUseCase,
    private val rearmTrip: RearmTripUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private var latestRearmTripId: String? = null

    init {
        viewModelScope.launch {
            observeHomeDashboard().collect { dashboard ->
                latestRearmTripId = dashboard.rearmTrip?.sourceTripId
                mutableState.value = HomeUiState(
                    headline = dashboard.headline,
                    statusLabel = dashboard.statusLabel,
                    statusTone = dashboard.statusTone.toUiTone(),
                    activeTrip = dashboard.activeTrip?.let { trip ->
                        HomeActiveTrip(
                            tripId = trip.tripId,
                            destinationName = trip.destinationName,
                            subtitle = trip.subtitle,
                        )
                    },
                    rearmTrip = dashboard.rearmTrip?.let { trip ->
                        HomeRearmTrip(
                            sourceTripId = trip.sourceTripId,
                            destinationName = trip.destinationName,
                            subtitle = trip.subtitle,
                        )
                    },
                    recentTrips = dashboard.recentTrips.map { trip ->
                        HomeRecentTrip(
                            tripId = trip.tripId,
                            destinationName = trip.destinationName,
                            subtitle = trip.subtitle,
                            statusLabel = trip.statusLabel,
                            statusTone = trip.statusTone.toUiTone(),
                        )
                    },
                )
            }
        }
    }

    fun rearmLastTrip(onStarted: (String) -> Unit) {
        viewModelScope.launch {
            val sourceTripId = latestRearmTripId ?: return@launch
            rearmTrip(sourceTripId)?.let(onStarted)
        }
    }
}

private fun HomeDashboardTone.toUiTone(): HomeStatusTone = when (this) {
    HomeDashboardTone.Safe -> HomeStatusTone.Safe
    HomeDashboardTone.Monitoring -> HomeStatusTone.Monitoring
    HomeDashboardTone.Approaching -> HomeStatusTone.Approaching
    HomeDashboardTone.Neutral -> HomeStatusTone.Neutral
}
