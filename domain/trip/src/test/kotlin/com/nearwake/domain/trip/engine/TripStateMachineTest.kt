package com.nearwake.domain.trip.engine

import com.google.common.truth.Truth.assertThat
import com.nearwake.domain.trip.model.TripState
import org.junit.jupiter.api.Test

class TripStateMachineTest {
    private val stateMachine = TripStateMachine()

    @Test
    fun `start trip arms the session and registers background hooks`() {
        val transition = stateMachine.transition(
            currentState = TripState.Idle,
            event = TripEvent.StartTrip,
        )

        assertThat(transition.toState).isEqualTo(TripState.Armed)
        assertThat(transition.sideEffects).containsExactly(
            TripSideEffect.PersistSession,
            TripSideEffect.RegisterGeofences,
            TripSideEffect.RegisterActivityTransitions,
        ).inOrder()
    }

    @Test
    fun `motion moves armed session into low power monitoring`() {
        val transition = stateMachine.transition(
            currentState = TripState.Armed,
            event = TripEvent.MotionDetected,
        )

        assertThat(transition.toState).isEqualTo(TripState.MonitoringLowPower)
        assertThat(transition.sideEffects).containsExactly(TripSideEffect.StartBalancedTracking)
    }

    @Test
    fun `approach signals escalate to precise monitoring`() {
        val transition = stateMachine.transition(
            currentState = TripState.MonitoringLowPower,
            event = TripEvent.ApproachWindowReached,
        )

        assertThat(transition.toState).isEqualTo(TripState.MonitoringApproach)
        assertThat(transition.sideEffects).containsExactly(TripSideEffect.StartPreciseBurst)
    }

    @Test
    fun `arrival signals trigger alerting`() {
        val transition = stateMachine.transition(
            currentState = TripState.MonitoringApproach,
            event = TripEvent.DestinationGeofenceEntered,
        )

        assertThat(transition.toState).isEqualTo(TripState.Alerting)
        assertThat(transition.sideEffects).containsExactly(
            TripSideEffect.StopLocationTracking,
            TripSideEffect.FireArrivalAlert,
        ).inOrder()
    }

    @Test
    fun `cancel works from any non terminal state`() {
        val transition = stateMachine.transition(
            currentState = TripState.MonitoringApproach,
            event = TripEvent.CancelTrip,
        )

        assertThat(transition.toState).isEqualTo(TripState.Cancelled)
        assertThat(transition.ignored).isFalse()
    }
}
