package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.TripState
import com.nearwake.domain.trip.model.isTerminal

class TripStateMachine {
    fun transition(currentState: TripState, event: TripEvent): TripTransition {
        if (event is TripEvent.CancelTrip && !currentState.isTerminal) {
            return TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.Cancelled,
                sideEffects = listOf(
                    TripSideEffect.StopLocationTracking,
                    TripSideEffect.CleanupMonitoring,
                ),
            )
        }

        if (event is TripEvent.RestoreMonitoring && !currentState.isTerminal) {
            return TripTransition(
                fromState = currentState,
                event = event,
                toState = currentState,
                sideEffects = listOf(TripSideEffect.RestorePersistedMonitoring),
            )
        }

        return when (currentState) {
            TripState.Idle -> handleIdle(event)
            TripState.Armed -> handleArmed(event)
            TripState.WaitingForMovement -> handleWaitingForMovement(event)
            TripState.MonitoringLowPower -> handleMonitoringLowPower(currentState, event)
            TripState.MonitoringApproach -> handleMonitoringApproach(currentState, event)
            TripState.Alerting -> handleAlerting(currentState, event)
            TripState.Recovery -> handleRecovery(currentState, event)
            TripState.Completed,
            TripState.Cancelled,
            TripState.FailedGracefully -> ignored(currentState, event)
        }
    }

    private fun handleIdle(event: TripEvent): TripTransition =
        when (event) {
            TripEvent.StartTrip -> TripTransition(
                fromState = TripState.Idle,
                event = event,
                toState = TripState.Armed,
                sideEffects = listOf(
                    TripSideEffect.PersistSession,
                    TripSideEffect.RegisterGeofences,
                    TripSideEffect.RegisterActivityTransitions,
                ),
            )

            else -> ignored(TripState.Idle, event)
        }

    private fun handleArmed(event: TripEvent): TripTransition =
        when (event) {
            TripEvent.MotionDetected -> TripTransition(
                fromState = TripState.Armed,
                event = event,
                toState = TripState.MonitoringLowPower,
                sideEffects = listOf(TripSideEffect.StartBalancedTracking),
            )

            TripEvent.ApproachGeofenceEntered -> TripTransition(
                fromState = TripState.Armed,
                event = event,
                toState = TripState.MonitoringApproach,
                sideEffects = listOf(TripSideEffect.StartPreciseBurst),
            )

            TripEvent.NoMotionTimeoutReached -> TripTransition(
                fromState = TripState.Armed,
                event = event,
                toState = TripState.WaitingForMovement,
                sideEffects = listOf(TripSideEffect.PromptForMovement),
            )

            TripEvent.NetworkLost -> TripTransition(
                fromState = TripState.Armed,
                event = event,
                toState = TripState.Armed,
                sideEffects = listOf(TripSideEffect.ApplyOfflineBias),
            )

            is TripEvent.NetworkRestored -> TripTransition(
                fromState = TripState.Armed,
                event = event,
                toState = TripState.Armed,
                sideEffects = listOf(TripSideEffect.ClearOfflineBias),
            )

            else -> ignored(TripState.Armed, event)
        }

    private fun handleWaitingForMovement(event: TripEvent): TripTransition =
        when (event) {
            TripEvent.MotionDetected,
            TripEvent.UserConfirmedMoving -> TripTransition(
                fromState = TripState.WaitingForMovement,
                event = event,
                toState = TripState.MonitoringLowPower,
                sideEffects = listOf(TripSideEffect.StartBalancedTracking),
            )

            else -> ignored(TripState.WaitingForMovement, event)
        }

    private fun handleMonitoringLowPower(currentState: TripState, event: TripEvent): TripTransition =
        when (event) {
            TripEvent.ApproachWindowReached,
            TripEvent.ApproachGeofenceEntered -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.MonitoringApproach,
                sideEffects = listOf(TripSideEffect.StartPreciseBurst),
            )

            TripEvent.NetworkLost -> TripTransition(
                fromState = currentState,
                event = event,
                toState = currentState,
                sideEffects = listOf(TripSideEffect.ApplyOfflineBias),
            )

            is TripEvent.NetworkRestored -> TripTransition(
                fromState = currentState,
                event = event,
                toState = currentState,
                sideEffects = listOf(TripSideEffect.ClearOfflineBias),
            )

            else -> ignored(currentState, event)
        }

    private fun handleMonitoringApproach(currentState: TripState, event: TripEvent): TripTransition =
        when (event) {
            TripEvent.DestinationGeofenceEntered,
            TripEvent.PreciseArrivalConfirmed -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.Alerting,
                sideEffects = listOf(
                    TripSideEffect.StopLocationTracking,
                    TripSideEffect.FireArrivalAlert,
                ),
            )

            TripEvent.NetworkLost -> TripTransition(
                fromState = currentState,
                event = event,
                toState = currentState,
                sideEffects = listOf(TripSideEffect.ApplyOfflineBias),
            )

            is TripEvent.NetworkRestored -> TripTransition(
                fromState = currentState,
                event = event,
                toState = currentState,
                sideEffects = listOf(TripSideEffect.ClearOfflineBias),
            )

            else -> ignored(currentState, event)
        }

    private fun handleAlerting(currentState: TripState, event: TripEvent): TripTransition =
        when (event) {
            TripEvent.AlertDismissed -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.Completed,
                sideEffects = listOf(
                    TripSideEffect.LogTripCompletion,
                    TripSideEffect.CleanupMonitoring,
                ),
            )

            TripEvent.AlertTimedOut -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.Recovery,
                sideEffects = listOf(TripSideEffect.StartRecoveryChecks),
            )

            else -> ignored(currentState, event)
        }

    private fun handleRecovery(currentState: TripState, event: TripEvent): TripTransition =
        when (event) {
            TripEvent.EndTrip -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.Completed,
                sideEffects = listOf(
                    TripSideEffect.LogTripCompletion,
                    TripSideEffect.CleanupMonitoring,
                ),
            )

            TripEvent.RerouteRequested -> TripTransition(
                fromState = currentState,
                event = event,
                toState = TripState.MonitoringLowPower,
                sideEffects = listOf(TripSideEffect.RestartMonitoring),
            )

            else -> ignored(currentState, event)
        }

    private fun ignored(state: TripState, event: TripEvent): TripTransition =
        TripTransition(
            fromState = state,
            event = event,
            toState = state,
            sideEffects = emptyList(),
            ignored = true,
        )
}
