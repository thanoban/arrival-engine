package com.nearwake.app

import androidx.lifecycle.ViewModel
import com.nearwake.application.trip.RearmTripUseCase
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.core.database.entity.TripEntity
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripState
import com.nearwake.domain.trip.model.isTerminal
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

data class HomeUiState(
    val headline: String = "Travel calmer on the rides that are easiest to miss.",
    val statusLabel: String = "Awaiting trip",
    val statusTone: HomeStatusTone = HomeStatusTone.Neutral,
    val activeTrip: HomeActiveTrip? = null,
    val rearmTrip: HomeRearmTrip? = null,
    val recentTrips: List<HomeRecentTrip> = emptyList(),
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
    private val tripDao: TripDao,
    private val tripSessionDao: TripSessionDao,
    private val savedPlaceDao: SavedPlaceDao,
    private val rearmTrip: RearmTripUseCase,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private var latestTripsById: Map<String, TripEntity> = emptyMap()
    private var latestPlacesById: Map<String, SavedPlaceEntity> = emptyMap()
    private var latestRearmTripId: String? = null

    init {
        scope.launch {
            combine(
                tripDao.observeTrips(),
                tripSessionDao.observeTripSessions(),
                savedPlaceDao.observeSavedPlaces(),
            ) { trips, sessions, places ->
                val tripsById = trips.associateBy { trip -> trip.id }
                val placesById = places.associateBy { place -> place.id }
                latestTripsById = tripsById
                latestPlacesById = placesById

                val activeSession = sessions.firstOrNull { session -> !session.state.isTerminal }
                val activeTrip = activeSession
                    ?.let { session -> tripsById[session.tripId]?.let { trip -> session to trip } }
                    ?.let { (session, trip) -> buildActiveTrip(session, trip, placesById[trip.destinationId]) }

                val rearmableTrip = trips.firstOrNull { trip ->
                    trip.completedAt != null && placesById[trip.destinationId] != null
                }
                latestRearmTripId = rearmableTrip?.id

                HomeUiState(
                    headline = when {
                        activeTrip != null -> "NearWake is already guarding your current ride."
                        rearmableTrip != null -> "Your last commute is ready to arm again."
                        else -> "Travel calmer on the rides that are easiest to miss."
                    },
                    statusLabel = when {
                        activeTrip != null -> "Monitoring now"
                        rearmableTrip != null -> "Ready to re-arm"
                        else -> "Awaiting trip"
                    },
                    statusTone = when {
                        activeTrip != null -> HomeStatusTone.Monitoring
                        rearmableTrip != null -> HomeStatusTone.Safe
                        else -> HomeStatusTone.Neutral
                    },
                    activeTrip = activeTrip,
                    rearmTrip = rearmableTrip?.let { trip ->
                        buildRearmTrip(trip = trip, place = placesById[trip.destinationId])
                    },
                    recentTrips = trips.mapNotNull { trip ->
                        buildRecentTrip(
                            trip = trip,
                            place = placesById[trip.destinationId],
                            session = sessions.firstOrNull { session -> session.tripId == trip.id },
                        )
                    }.take(5),
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun rearmLastTrip(onStarted: (String) -> Unit) {
        scope.launch {
            val sourceTripId = latestRearmTripId ?: return@launch
            rearmTrip(sourceTripId)?.let(onStarted)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    private fun buildActiveTrip(
        session: TripSessionEntity,
        trip: TripEntity,
        place: SavedPlaceEntity?,
    ): HomeActiveTrip? {
        place ?: return null
        val confidenceLabel = when (session.confidence) {
            Confidence.HIGH -> "High confidence"
            Confidence.DEGRADED -> "Medium confidence"
            Confidence.OFFLINE -> "Low confidence"
        }
        val stageLabel = when (session.alertStage) {
            AlertStage.APPROACH -> "Approach window"
            AlertStage.IMMINENT -> "Get ready"
            AlertStage.ARRIVAL -> "Arrival alert"
            AlertStage.RECOVERY -> "Recovery mode"
            else -> "Monitoring"
        }
        val etaLabel = session.lastEtaMinutes?.let { minutes -> "~$minutes min" } ?: "ETA updating"
        return HomeActiveTrip(
            tripId = trip.id,
            destinationName = place.name,
            subtitle = "$stageLabel · $etaLabel · $confidenceLabel",
        )
    }

    private fun buildRearmTrip(
        trip: TripEntity,
        place: SavedPlaceEntity?,
    ): HomeRearmTrip? {
        place ?: return null
        val leadLabel = formatAlertPreference(
            triggerMode = trip.alertTriggerMode,
            alertLeadMinutes = trip.alertLeadMinutes,
            alertDistanceMeters = trip.alertDistanceMeters,
        )
        val intensityLabel = trip.alertIntensity.name.lowercase().replaceFirstChar(Char::uppercase)
        return HomeRearmTrip(
            sourceTripId = trip.id,
            destinationName = place.name,
            subtitle = "$leadLabel · $intensityLabel",
        )
    }

    private fun buildRecentTrip(
        trip: TripEntity,
        place: SavedPlaceEntity?,
        session: TripSessionEntity?,
    ): HomeRecentTrip? {
        place ?: return null
        val status = when {
            session != null && !session.state.isTerminal -> "Monitoring now"
            trip.completedAt != null -> "Completed"
            else -> "Ready to re-arm"
        }
        val tone = when (status) {
            "Completed" -> HomeStatusTone.Safe
            "Monitoring now" -> HomeStatusTone.Monitoring
            else -> HomeStatusTone.Approaching
        }
        val timestamp = trip.createdAt.toString().replace('T', ' ').take(16)
        val lead = formatAlertPreference(
            triggerMode = trip.alertTriggerMode,
            alertLeadMinutes = trip.alertLeadMinutes,
            alertDistanceMeters = trip.alertDistanceMeters,
        )
        return HomeRecentTrip(
            tripId = trip.id,
            destinationName = place.name,
            subtitle = "$lead · $timestamp",
            statusLabel = status,
            statusTone = tone,
        )
    }

    private fun formatAlertPreference(
        triggerMode: AlertTriggerMode,
        alertLeadMinutes: Int,
        alertDistanceMeters: Int,
    ): String = when (triggerMode) {
        AlertTriggerMode.TIME ->
            if (alertLeadMinutes == 0) "Nearby alert" else "${alertLeadMinutes} min early"
        AlertTriggerMode.DISTANCE -> "${alertDistanceMeters.toDistanceLabel()} away"
        AlertTriggerMode.BOTH -> buildString {
            append(if (alertLeadMinutes == 0) "Nearby" else "${alertLeadMinutes} min")
            append(" or ")
            append(alertDistanceMeters.toDistanceLabel())
        }
    }

    private fun Int.toDistanceLabel(): String =
        if (this >= 1000) {
            val kilometers = this / 1000.0
            if (kilometers % 1.0 == 0.0) "${kilometers.toInt()} km" else "${kilometers} km"
        } else {
            "$this m"
        }
}
