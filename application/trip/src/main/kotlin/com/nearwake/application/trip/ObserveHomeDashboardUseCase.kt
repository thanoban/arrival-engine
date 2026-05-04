package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.isTerminal
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class HomeDashboard(
    val headline: String,
    val statusLabel: String,
    val statusTone: HomeDashboardTone,
    val activeTrip: HomeDashboardActiveTrip? = null,
    val rearmTrip: HomeDashboardRearmTrip? = null,
    val recentTrips: List<HomeDashboardRecentTrip> = emptyList(),
)

data class HomeDashboardActiveTrip(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
)

data class HomeDashboardRearmTrip(
    val sourceTripId: String,
    val destinationName: String,
    val subtitle: String,
)

data class HomeDashboardRecentTrip(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
    val statusLabel: String,
    val statusTone: HomeDashboardTone,
)

enum class HomeDashboardTone {
    Safe,
    Monitoring,
    Approaching,
    Neutral,
}

class ObserveHomeDashboardUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(): Flow<HomeDashboard> =
        tripLifecycleStore.observeHomeSnapshot().map { snapshot ->
            val tripsById = snapshot.trips.associateBy { trip -> trip.id }
            val placesById = snapshot.savedPlaces.associateBy { place -> place.id }
            val sessionsByTripId = snapshot.sessions.associateBy { session -> session.tripId }

            val activeSession = snapshot.sessions.firstOrNull { session -> !session.state.isTerminal }
            val activeTrip = activeSession
                ?.let { session -> tripsById[session.tripId]?.let { trip -> session to trip } }
                ?.let { (session, trip) -> buildActiveTrip(session, trip, placesById[trip.destinationId]) }

            val rearmableTrip = snapshot.trips.firstOrNull { trip ->
                trip.completedAt != null && placesById[trip.destinationId] != null
            }

            HomeDashboard(
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
                    activeTrip != null -> HomeDashboardTone.Monitoring
                    rearmableTrip != null -> HomeDashboardTone.Safe
                    else -> HomeDashboardTone.Neutral
                },
                activeTrip = activeTrip,
                rearmTrip = rearmableTrip?.let { trip ->
                    buildRearmTrip(trip = trip, place = placesById[trip.destinationId])
                },
                recentTrips = snapshot.trips.mapNotNull { trip ->
                    buildRecentTrip(
                        trip = trip,
                        place = placesById[trip.destinationId],
                        session = sessionsByTripId[trip.id],
                    )
                }.take(5),
            )
        }

    private fun buildActiveTrip(
        session: PersistedTripSession,
        trip: PersistedTrip,
        place: PersistedSavedPlace?,
    ): HomeDashboardActiveTrip? {
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
        return HomeDashboardActiveTrip(
            tripId = trip.id,
            destinationName = place.name,
            subtitle = "$stageLabel · $etaLabel · $confidenceLabel",
        )
    }

    private fun buildRearmTrip(
        trip: PersistedTrip,
        place: PersistedSavedPlace?,
    ): HomeDashboardRearmTrip? {
        place ?: return null
        val leadLabel = formatAlertPreference(
            triggerMode = trip.alertTriggerMode,
            alertLeadMinutes = trip.alertLeadMinutes,
            alertDistanceMeters = trip.alertDistanceMeters,
        )
        val intensityLabel = trip.alertIntensity.name.lowercase().replaceFirstChar(Char::uppercase)
        return HomeDashboardRearmTrip(
            sourceTripId = trip.id,
            destinationName = place.name,
            subtitle = "$leadLabel · $intensityLabel",
        )
    }

    private fun buildRecentTrip(
        trip: PersistedTrip,
        place: PersistedSavedPlace?,
        session: PersistedTripSession?,
    ): HomeDashboardRecentTrip? {
        place ?: return null
        val status = when {
            session != null && !session.state.isTerminal -> "Monitoring now"
            trip.completedAt != null -> "Completed"
            else -> "Ready to re-arm"
        }
        val tone = when (status) {
            "Completed" -> HomeDashboardTone.Safe
            "Monitoring now" -> HomeDashboardTone.Monitoring
            else -> HomeDashboardTone.Approaching
        }
        val timestamp = trip.createdAt.toString().replace('T', ' ').take(16)
        val lead = formatAlertPreference(
            triggerMode = trip.alertTriggerMode,
            alertLeadMinutes = trip.alertLeadMinutes,
            alertDistanceMeters = trip.alertDistanceMeters,
        )
        return HomeDashboardRecentTrip(
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
