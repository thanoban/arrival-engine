package com.nearwake.application.trip

import com.nearwake.domain.routing.model.RouteSignalQuality
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.repository.RoutingRepository
import com.nearwake.domain.trip.engine.TransferCheckpointType
import com.nearwake.domain.trip.engine.TransferMonitor
import com.nearwake.domain.trip.engine.TransferProgressStatus as DomainTransferProgressStatus
import com.nearwake.domain.trip.model.AlertMode
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

data class LiveTripPresentation(
    val tripId: String,
    val destinationName: String,
    val etaLabel: String,
    val routeSummary: String,
    val elapsedTimeLabel: String,
    val monitoringMode: MonitoringMode,
    val confidence: Confidence,
    val alertSummary: String,
    val alertMode: AlertMode,
    val alertStage: AlertStage,
    val transferSteps: List<LiveTripTransferStep>,
)

data class LiveTripTransferStep(
    val title: String,
    val subtitle: String,
    val timingLabel: String,
    val status: LiveTripTransferStatus,
    val signalQuality: RouteSignalQuality = RouteSignalQuality.HIGH,
)

enum class LiveTripTransferStatus {
    Completed,
    Soon,
    Upcoming,
    Final,
}

class ObserveLiveTripUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
    private val routingRepository: RoutingRepository,
) {
    operator fun invoke(tripId: String): Flow<LiveTripPresentation> =
        tripLifecycleStore.observeHomeSnapshot().mapLatest { snapshot ->
            val trip = snapshot.trips.firstOrNull { persistedTrip -> persistedTrip.id == tripId }
            val session = snapshot.sessions.firstOrNull { persistedSession -> persistedSession.tripId == tripId }
            val place = trip?.destinationId?.let { destinationId ->
                snapshot.savedPlaces.firstOrNull { persistedPlace -> persistedPlace.id == destinationId }
            }
            val routeSnapshot = routingRepository.getCachedRoute(tripId)
            val minutes = session?.lastEtaMinutes ?: 22

            LiveTripPresentation(
                tripId = tripId,
                destinationName = place?.name ?: "Live trip",
                etaLabel = "~${minutes} min",
                routeSummary = routeSnapshot?.toRouteSummary() ?: "Destination-only monitoring",
                elapsedTimeLabel = trip?.createdAt?.toReadableLabel().orEmpty(),
                monitoringMode = session?.monitoringMode ?: MonitoringMode.GEOFENCE_ONLY,
                confidence = session?.confidence ?: Confidence.HIGH,
                alertMode = trip?.alertMode ?: AlertMode.ACTIVE,
                alertStage = session?.alertStage ?: AlertStage.MONITORING,
                transferSteps = routeSnapshot?.let { snapshotRoute ->
                    buildTransferProgress(
                        routeSnapshot = snapshotRoute,
                        etaMinutes = minutes,
                        destinationName = place?.name ?: "Destination",
                    )
                }.orEmpty(),
                alertSummary = trip?.let { configuredTrip ->
                    val lead = if (configuredTrip.alertLeadMinutes == 0) {
                        "Nearby"
                    } else {
                        "${configuredTrip.alertLeadMinutes} min early"
                    }
                    "$lead · ${configuredTrip.alertIntensity.name.lowercase().replaceFirstChar(Char::uppercase)} · ${configuredTrip.alertMode.name.lowercase().replaceFirstChar(Char::uppercase)} mode"
                }.orEmpty(),
            )
        }

    private fun buildTransferProgress(
        routeSnapshot: RouteSnapshot,
        etaMinutes: Int,
        destinationName: String,
    ): List<LiveTripTransferStep> {
        val transferState = TransferMonitor().evaluate(
            routeSnapshot = routeSnapshot,
            etaMinutes = etaMinutes,
            destinationName = destinationName,
        )

        return transferState.checkpoints.map { checkpoint ->
            when (checkpoint.type) {
                TransferCheckpointType.TRANSFER -> LiveTripTransferStep(
                    title = if (checkpoint.status == DomainTransferProgressStatus.COMPLETED) {
                        "Changed to ${checkpoint.lineName.orEmpty()}"
                    } else {
                        "Change to ${checkpoint.lineName.orEmpty()}"
                    },
                    subtitle = checkpoint.stopName,
                    timingLabel = when {
                        checkpoint.status == DomainTransferProgressStatus.COMPLETED -> "Passed"
                        checkpoint.remainingMinutes <= 1 -> "Now"
                        else -> "in ${checkpoint.remainingMinutes} min"
                    },
                    status = when (checkpoint.status) {
                        DomainTransferProgressStatus.COMPLETED -> LiveTripTransferStatus.Completed
                        DomainTransferProgressStatus.SOON -> LiveTripTransferStatus.Soon
                        DomainTransferProgressStatus.UPCOMING -> LiveTripTransferStatus.Upcoming
                    },
                    signalQuality = checkpoint.signalQuality,
                )

                TransferCheckpointType.DESTINATION -> LiveTripTransferStep(
                    title = "Arrive at ${checkpoint.stopName}",
                    subtitle = "Final stop",
                    timingLabel = if (checkpoint.remainingMinutes <= 1) "Now" else "in ${checkpoint.remainingMinutes} min",
                    status = LiveTripTransferStatus.Final,
                )
            }
        }
    }
}
