package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CompanionPresentation(
    val tripId: String,
    val title: String,
    val messagePreview: String,
    val smsPreview: String,
)

class ObserveCompanionUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(tripId: String): Flow<CompanionPresentation> =
        tripLifecycleStore.observeHomeSnapshot().mapLatest { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            val completedAt = trip?.completedAt?.toLocalDateTime(TimeZone.currentSystemDefault())
            val timeLabel = completedAt?.let { local ->
                "%02d:%02d".format(local.hour, local.minute)
            } ?: "just now"
            val destinationName = place?.name ?: "my destination"
            val shareMessage = "I arrived at $destinationName at $timeLabel."

            CompanionPresentation(
                tripId = tripId,
                title = "Arrival confirmed",
                messagePreview = shareMessage,
                smsPreview = shareMessage,
            )
        }
}
