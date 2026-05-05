package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AlertPresentation(
    val tripId: String,
    val destinationName: String,
    val etaLabel: String,
)

class ObserveAlertUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(tripId: String): Flow<AlertPresentation> =
        tripLifecycleStore.observeHomeSnapshot().map { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val session = snapshot.sessions.firstOrNull { persistedSession -> persistedSession.tripId == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            AlertPresentation(
                tripId = tripId,
                destinationName = place?.name ?: "Arrival alert",
                etaLabel = session?.lastEtaMinutes?.let { minutes -> "~$minutes min away" } ?: "You are close",
            )
        }
}
