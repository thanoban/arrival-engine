package com.nearwake.data.alerts

import android.app.Notification
import com.nearwake.data.analytics.DiagnosticsLogger
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.trip.engine.BoardingAlignment
import com.nearwake.domain.trip.engine.BoardingValidator
import com.nearwake.domain.trip.engine.TransferMonitor
import com.nearwake.domain.trip.engine.TransferProgressStatus
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.TripSession
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TripMonitoringFeedbackCoordinator @Inject constructor(
    private val notificationHelper: NotificationHelper,
    private val diagnosticsLogger: DiagnosticsLogger,
    private val tripMonitoringRuntime: TripMonitoringRuntime,
) {
    private val boardingValidator = BoardingValidator()
    private val transferMonitor = TransferMonitor()
    private var boardingValidationComplete = false
    private var lastTransferCueKey: String? = null

    fun reset() {
        boardingValidationComplete = false
        lastTransferCueKey = null
    }

    fun refreshMonitoringNotification(
        session: TripSession?,
        context: MonitoredTripContext?,
    ) {
        notificationHelper.notify(
            NOTIFICATION_ID_MONITORING,
            notificationHelper.buildMonitoringNotification(
                mode = session?.monitoringMode ?: MonitoringMode.GEOFENCE_ONLY,
                stage = session?.alertStage ?: AlertStage.MONITORING,
                destinationName = context?.destinationName,
                etaMinutes = session?.lastEtaMinutes,
                confidence = session?.confidence,
                alertMode = context?.alertMode,
            ),
        )
    }

    suspend fun onSessionUpdated(
        previousSession: TripSession?,
        currentSession: TripSession,
        context: MonitoredTripContext,
        sampleLocation: LatLng? = null,
    ) {
        maybeNotifyStageAdvance(previousSession, currentSession, context)
        maybeNotifyTransferCue(currentSession, context)
        if (sampleLocation != null && previousSession != null) {
            maybeNotifyBoardingMismatch(
                previousSession = previousSession,
                currentSession = currentSession,
                currentLocation = sampleLocation,
                context = context,
            )
        }
    }

    suspend fun resolveAlertDistanceMeters(
        session: TripSession,
        context: MonitoredTripContext,
    ): Double? {
        val lastKnownLocation = lastKnownLocationOrNull(session)
        if (lastKnownLocation == null) {
            diagnosticsLogger.log(
                eventType = "alert_distance_unavailable",
                tripId = session.tripId,
                payload = buildJsonObject {
                    put("reason", "missing_last_known_location")
                    put("stage", session.alertStage.name)
                },
            )
            return null
        }

        return tripMonitoringRuntime.distanceToDestination(
            lat = lastKnownLocation.lat,
            lng = lastKnownLocation.lng,
            destination = context.destination,
        )
    }

    private fun maybeNotifyStageAdvance(
        previousSession: TripSession?,
        currentSession: TripSession,
        context: MonitoredTripContext,
    ) {
        val previousStage = previousSession?.alertStage ?: AlertStage.MONITORING
        val currentStage = currentSession.alertStage
        if (currentStage.ordinal <= previousStage.ordinal) {
            return
        }
        if (currentStage == AlertStage.ARRIVAL || currentStage == AlertStage.RECOVERY) {
            return
        }
        val notificationId = if (currentStage == AlertStage.IMMINENT) {
            NOTIFICATION_ID_IMMINENT
        } else {
            NOTIFICATION_ID_APPROACH
        }
        notificationHelper.notify(
            notificationId,
            notificationHelper.buildStageNotification(
                tripId = context.tripId,
                destinationName = context.destinationName,
                stage = currentStage,
                mode = context.alertMode,
            ),
        )
    }

    private suspend fun maybeNotifyTransferCue(
        session: TripSession,
        context: MonitoredTripContext,
    ) {
        val routeSnapshot = context.routeSnapshot ?: return
        val etaMinutes = session.lastEtaMinutes ?: return
        val nextTransfer = transferMonitor.evaluate(
            routeSnapshot = routeSnapshot,
            etaMinutes = etaMinutes,
            destinationName = context.destinationName,
        ).nextTransfer ?: return

        if (nextTransfer.status != TransferProgressStatus.SOON) {
            return
        }

        val cueKey = "${nextTransfer.stopName}:${nextTransfer.lineName.orEmpty()}"
        if (lastTransferCueKey == cueKey) {
            return
        }
        lastTransferCueKey = cueKey

        notificationHelper.notify(
            NOTIFICATION_ID_TRANSFER,
            notificationHelper.buildTransferNotification(
                tripId = context.tripId,
                stopName = nextTransfer.stopName,
                lineName = nextTransfer.lineName.orEmpty(),
                remainingMinutes = nextTransfer.remainingMinutes,
            ),
        )
        diagnosticsLogger.log(
            eventType = "transfer_alert_fired",
            tripId = context.tripId,
            payload = buildJsonObject {
                put("stop_name", nextTransfer.stopName)
                put("line_name", nextTransfer.lineName.orEmpty())
                put("remaining_minutes", nextTransfer.remainingMinutes)
            },
        )
    }

    private suspend fun maybeNotifyBoardingMismatch(
        previousSession: TripSession,
        currentSession: TripSession,
        currentLocation: LatLng,
        context: MonitoredTripContext,
    ) {
        if (boardingValidationComplete) {
            return
        }
        if (currentSession.alertStage != AlertStage.MONITORING &&
            currentSession.alertStage != AlertStage.APPROACH
        ) {
            boardingValidationComplete = true
            return
        }

        val previousLocation = lastKnownLocationOrNull(previousSession) ?: return
        val routeSnapshot = context.routeSnapshot ?: return
        val result = boardingValidator.evaluate(
            routeSnapshot = routeSnapshot,
            previousLocation = previousLocation,
            currentLocation = currentLocation,
        )

        when (result.alignment) {
            BoardingAlignment.UNDETERMINED -> Unit
            BoardingAlignment.ALIGNED -> {
                boardingValidationComplete = true
                diagnosticsLogger.log(
                    eventType = "boarding_direction_confirmed",
                    tripId = currentSession.tripId,
                    payload = buildJsonObject {
                        put("heading_delta_degrees", result.headingDeltaDegrees ?: -1.0)
                    },
                )
            }

            BoardingAlignment.WRONG_DIRECTION -> {
                boardingValidationComplete = true
                notificationHelper.notify(
                    NOTIFICATION_ID_BOARDING_WARNING,
                    notificationHelper.buildBoardingWarningNotification(
                        tripId = context.tripId,
                        destinationName = context.destinationName,
                    ),
                )
                diagnosticsLogger.log(
                    eventType = "boarding_direction_warning",
                    tripId = currentSession.tripId,
                    payload = buildJsonObject {
                        put("expected_heading_degrees", result.expectedHeadingDegrees ?: -1.0)
                        put("actual_heading_degrees", result.actualHeadingDegrees ?: -1.0)
                        put("heading_delta_degrees", result.headingDeltaDegrees ?: -1.0)
                    },
                )
            }
        }
    }

    private fun lastKnownLocationOrNull(session: TripSession): LatLng? {
        val lat = session.lastKnownLat
        val lng = session.lastKnownLng
        return if (lat != null && lng != null) {
            LatLng(lat = lat, lng = lng)
        } else {
            null
        }
    }

    companion object {
        private const val NOTIFICATION_ID_MONITORING = 41
        private const val NOTIFICATION_ID_APPROACH = 43
        private const val NOTIFICATION_ID_TRANSFER = 44
        private const val NOTIFICATION_ID_BOARDING_WARNING = 45
        private const val NOTIFICATION_ID_IMMINENT = 46
    }
}
