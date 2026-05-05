package com.nearwake.application.trip

import com.nearwake.domain.trip.model.AlertTriggerMode
import com.nearwake.domain.trip.model.isTerminal
import com.nearwake.ports.persistence.PersistedSavedPlace
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class TripHistoryItem(
    val tripId: String,
    val destinationName: String,
    val subtitle: String,
    val statusLabel: String,
)

data class TripHistorySummary(
    val trips: List<TripHistoryItem>,
)

class ObserveTripHistoryUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(): Flow<TripHistorySummary> =
        tripLifecycleStore.observeHomeSnapshot().map { snapshot ->
            val placesById = snapshot.savedPlaces.associateBy { place -> place.id }
            val activeTripIds = snapshot.sessions
                .filterNot { session -> session.state.isTerminal }
                .map { session -> session.tripId }
                .toSet()

            TripHistorySummary(
                trips = snapshot.trips.map { trip ->
                    trip.toHistoryItem(
                        place = placesById[trip.destinationId],
                        isActive = trip.id in activeTripIds,
                    )
                },
            )
        }
}

internal fun PersistedTrip.toHistoryItem(
    place: PersistedSavedPlace?,
    isActive: Boolean,
): TripHistoryItem =
    TripHistoryItem(
        tripId = id,
        destinationName = place?.name ?: "Unknown destination",
        subtitle = place?.address ?: "Saved destination",
        statusLabel = when {
            completedAt != null -> "Completed"
            isActive -> "Monitoring now"
            else -> "Ended early"
        },
    )

internal fun PersistedTrip.toAlertPreferenceLabel(): String = when (alertTriggerMode) {
    AlertTriggerMode.TIME ->
        if (alertLeadMinutes == 0) "Alert when nearby" else "Alert $alertLeadMinutes minutes early"
    AlertTriggerMode.DISTANCE -> "Alert ${alertDistanceMeters.toDistanceLabel()} away"
    AlertTriggerMode.BOTH -> buildString {
        append("Alert ")
        append(if (alertLeadMinutes == 0) "nearby" else "$alertLeadMinutes minutes early")
        append(" or ")
        append(alertDistanceMeters.toDistanceLabel())
        append(" away")
    }
}

internal fun Int.toDistanceLabel(): String =
    if (this >= 1000) {
        val kilometers = this / 1000.0
        if (kilometers % 1.0 == 0.0) "${kilometers.toInt()} km" else "${kilometers} km"
    } else {
        "$this m"
    }

internal fun PersistedTripSession?.toStatusLabel(trip: PersistedTrip): String = when {
    trip.completedAt != null -> "Completed"
    this != null && !state.isTerminal -> "Monitoring in progress"
    else -> "Ended early"
}
