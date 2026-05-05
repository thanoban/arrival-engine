package com.nearwake.application.trip

import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.ports.persistence.PersistedTrip
import com.nearwake.ports.persistence.PersistedTripSession
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

data class TripSummary(
    val destinationName: String,
    val destinationAddress: String,
    val statusLabel: String,
    val startedLabel: String,
    val alertLeadLabel: String,
    val alertIntensityLabel: String,
    val monitoringLabel: String,
    val routeSummary: String,
    val etaLabel: String,
    val confidenceLabel: String,
)

class ObserveTripSummaryUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val routingRepository: RoutingRepository,
) {
    operator fun invoke(tripId: String): Flow<TripSummary> =
        tripLifecycleStore.observeHomeSnapshot().mapLatest { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val session = snapshot.sessions.firstOrNull { persistedSession -> persistedSession.tripId == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            val routeSnapshot = routingRepository.getCachedRoute(tripId)

            TripSummary(
                destinationName = place?.name ?: "Unknown destination",
                destinationAddress = place?.address.orEmpty(),
                statusLabel = resolveStatusLabel(session, trip),
                startedLabel = trip?.createdAt?.toReadableLabel().orEmpty(),
                alertLeadLabel = trip?.toAlertPreferenceLabel().orEmpty(),
                alertIntensityLabel = trip?.alertIntensity?.name?.toDisplayLabel().orEmpty(),
                monitoringLabel = session?.monitoringMode?.name?.toDisplayLabel() ?: "Not monitoring",
                routeSummary = routeSnapshot?.toRouteSummary() ?: "Destination-only monitoring",
                etaLabel = session?.lastEtaMinutes?.let { minutes -> "~$minutes min at last check" }.orEmpty(),
                confidenceLabel = session?.confidence?.toConfidenceLabel() ?: "No confidence data",
            )
        }

    private fun resolveStatusLabel(session: PersistedTripSession?, trip: PersistedTrip?): String = when {
        trip == null -> "Missing"
        else -> session.toStatusLabel(trip)
    }
}

internal fun Confidence.toConfidenceLabel(): String = when (this) {
    Confidence.HIGH -> "High confidence"
    Confidence.DEGRADED -> "Medium confidence"
    Confidence.OFFLINE -> "Low confidence"
}

internal fun String.toDisplayLabel(): String =
    lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase)

internal fun kotlinx.datetime.Instant.toReadableLabel(): String =
    toString().replace('T', ' ').take(16)

internal fun RouteSnapshot.toRouteSummary(): String {
    val stopLabel = if (stops.size == 1) "1 stop" else "${stops.size} stops"
    val transferLabel = if (transfers.size == 1) "1 transfer" else "${transfers.size} transfers"
    return "$stopLabel · $transferLabel"
}
