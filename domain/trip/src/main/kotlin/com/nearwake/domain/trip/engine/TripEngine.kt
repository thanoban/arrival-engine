package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.Confidence
import com.nearwake.domain.trip.model.MonitoringMode
import com.nearwake.domain.trip.model.Trip
import com.nearwake.domain.trip.model.AlertStage
import com.nearwake.domain.trip.model.TripRule
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import kotlinx.datetime.Clock

data class TripEngineResult(
    val session: TripSession,
    val transition: TripTransition,
    val approachDecision: ApproachDecision? = null,
    val alertDecisionOutcome: AlertDecisionOutcome? = null,
)

class TripEngine(
    private val stateMachine: TripStateMachine = TripStateMachine(),
    private val approachEvaluator: ApproachEvaluator = ApproachEvaluator(),
    private val overshootDetector: OvershootDetector = OvershootDetector(),
    private val alertDecisionEngine: AlertDecisionEngine = AlertDecisionEngine(),
    private val clock: Clock = Clock.System,
) {
    fun armTrip(
        trip: Trip,
        geofenceIds: List<String> = emptyList(),
    ): TripEngineResult {
        val session = TripSession(
            tripId = trip.id,
            state = TripState.Idle,
            monitoringMode = MonitoringMode.GEOFENCE_ONLY,
            alertStage = AlertStage.MONITORING,
            geofenceIds = geofenceIds,
            updatedAt = clock.now(),
        )
        return onEvent(session = session, event = TripEvent.StartTrip)
    }

    fun onEvent(session: TripSession, event: TripEvent): TripEngineResult {
        val transition = stateMachine.transition(session.state, event)
        val updatedSession = session.copy(
            state = transition.toState,
            monitoringMode = transition.toState.toMonitoringMode(current = session.monitoringMode),
            confidence = event.updatedConfidence(current = session.confidence),
            updatedAt = clock.now(),
        )
        return TripEngineResult(
            session = updatedSession,
            transition = transition,
        )
    }

    fun restoreSession(session: TripSession): TripEngineResult =
        onEvent(session = session, event = TripEvent.RestoreMonitoring)

    fun evaluateApproach(
        session: TripSession,
        tripRule: TripRule,
        etaMinutes: Int?,
        distanceMeters: Double?,
    ): TripEngineResult {
        val decision = approachEvaluator.evaluate(
            etaMinutes = etaMinutes,
            distanceMeters = distanceMeters,
            tripRule = tripRule,
            confidence = session.confidence,
        )
        val seededSession = session.copy(
            lastEtaMinutes = etaMinutes,
            updatedAt = clock.now(),
        )
        return if (decision.shouldEscalate) {
            onEvent(session = seededSession, event = TripEvent.ApproachWindowReached).copy(
                approachDecision = decision,
            )
        } else {
            TripEngineResult(
                session = seededSession,
                transition = TripTransition(
                    fromState = session.state,
                    event = TripEvent.ApproachWindowReached,
                    toState = session.state,
                    sideEffects = emptyList(),
                    ignored = true,
                ),
                approachDecision = decision,
            )
        }
    }

    fun evaluateAlert(
        session: TripSession,
        tripRule: TripRule,
        distanceMeters: Double?,
        previousDistanceMeters: Double?,
        destinationGeofenceEntered: Boolean,
    ): TripEngineResult {
        val overshot = overshootDetector.hasOvershot(
            previousDistanceMeters = previousDistanceMeters,
            currentDistanceMeters = distanceMeters,
            tripRule = tripRule,
        )
        val outcome = alertDecisionEngine.decide(
            tripId = session.tripId,
            rule = tripRule,
            confidence = session.confidence,
            distanceMeters = distanceMeters,
            destinationGeofenceEntered = destinationGeofenceEntered,
            overshot = overshot,
        )

        return when (outcome.action) {
            AlertAction.ALERT -> {
                val event = if (destinationGeofenceEntered) {
                    TripEvent.DestinationGeofenceEntered
                } else {
                    TripEvent.PreciseArrivalConfirmed
                }
                onEvent(session = session, event = event).copy(alertDecisionOutcome = outcome)
            }

            AlertAction.RECOVER,
            AlertAction.HOLD -> TripEngineResult(
                session = session.copy(updatedAt = clock.now()),
                transition = TripTransition(
                    fromState = session.state,
                    event = TripEvent.PreciseArrivalConfirmed,
                    toState = session.state,
                    sideEffects = emptyList(),
                    ignored = true,
                ),
                alertDecisionOutcome = outcome,
            )
        }
    }

    private fun TripEvent.updatedConfidence(current: Confidence): Confidence =
        when (this) {
            TripEvent.NetworkLost -> Confidence.OFFLINE
            is TripEvent.NetworkRestored -> confidence
            else -> current
        }

    private fun TripState.toMonitoringMode(current: MonitoringMode): MonitoringMode =
        when (this) {
            TripState.MonitoringLowPower,
            TripState.Recovery -> MonitoringMode.BALANCED

            TripState.MonitoringApproach -> MonitoringMode.PRECISE_BURST

            TripState.Idle,
            TripState.Armed,
            TripState.WaitingForMovement,
            TripState.Alerting,
            TripState.Completed,
            TripState.Cancelled,
            TripState.FailedGracefully -> if (this == TripState.Idle) current else MonitoringMode.GEOFENCE_ONLY
        }
}
