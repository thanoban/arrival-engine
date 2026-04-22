package com.nearwake.domain.trip.model

import kotlinx.serialization.Serializable

@Serializable
enum class TripState {
    Idle,
    Armed,
    WaitingForMovement,
    MonitoringLowPower,
    MonitoringApproach,
    Alerting,
    Recovery,
    Completed,
    Cancelled,
    FailedGracefully,
}

val TripState.isTerminal: Boolean
    get() = this == TripState.Completed || this == TripState.Cancelled || this == TripState.FailedGracefully
