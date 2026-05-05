package com.nearwake.application.trip

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.engine.RecoveryGuidanceMode
import com.nearwake.domain.trip.engine.RecoveryPlanner
import com.nearwake.domain.trip.model.isTerminal
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

data class RecoveryPresentation(
    val tripId: String,
    val destinationName: String,
    val missedByLabel: String,
    val routeSummary: String,
    val lastEtaLabel: String,
    val confidenceLabel: String,
    val recoveryGuidanceLabel: String,
    val returnStopLabel: String? = null,
    val walkBackLabel: String? = null,
    val canResumeMonitoring: Boolean,
)

class ObserveRecoveryUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val routingRepository: RoutingRepository,
) {
    private val recoveryPlanner = RecoveryPlanner()

    operator fun invoke(tripId: String): Flow<RecoveryPresentation> =
        tripLifecycleStore.observeHomeSnapshot().mapLatest { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val session = snapshot.sessions.firstOrNull { persistedSession -> persistedSession.tripId == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            val routeSnapshot = routingRepository.getCachedRoute(tripId)
            val currentLocation = session?.lastKnownLat?.let { lat ->
                session.lastKnownLng?.let { lng ->
                    LatLng(lat = lat, lng = lng)
                }
            }
            val recoveryPlan = place?.let { destination ->
                recoveryPlanner.plan(
                    currentLocation = currentLocation,
                    destinationLocation = LatLng(destination.lat, destination.lng),
                    routeSnapshot = routeSnapshot,
                )
            }

            RecoveryPresentation(
                tripId = tripId,
                destinationName = place?.name ?: "Recovery",
                missedByLabel = session?.updatedAt?.toReadableLabel() ?: "moments ago",
                routeSummary = routeSnapshot?.toRouteSummary() ?: "Destination-only monitoring",
                lastEtaLabel = session?.lastEtaMinutes?.let { minutes -> "~$minutes min at last check" }
                    ?: "Last ETA unavailable",
                confidenceLabel = session?.confidence?.toConfidenceLabel() ?: "Confidence unknown",
                recoveryGuidanceLabel = when (recoveryPlan?.guidanceMode) {
                    RecoveryGuidanceMode.WALK_BACK -> "You are still close enough to recover on foot."
                    RecoveryGuidanceMode.RETURN_STOP ->
                        "Get off at the next safe moment and head for the nearest return stop."
                    RecoveryGuidanceMode.RESUME_MONITORING, null ->
                        "Resume monitoring and check the next safe stop."
                },
                returnStopLabel = recoveryPlan?.returnStopName?.let { stopName ->
                    val distanceLabel = recoveryPlan.returnStopDistanceMeters?.let { distance ->
                        "about ${distance}m away"
                    } ?: "nearby"
                    "Nearest return stop: $stopName ($distanceLabel)"
                },
                walkBackLabel = recoveryPlan?.walkBackDistanceMeters?.let { distance ->
                    "You are about ${distance}m from ${place?.name ?: "the destination"}. If it is safe, stop now and walk back."
                },
                canResumeMonitoring = session != null && !session.state.isTerminal,
            )
        }
}
