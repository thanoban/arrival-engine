package com.nearwake.domain.trip.engine

import com.nearwake.domain.trip.model.TripState

data class TripTransition(
    val fromState: TripState,
    val event: TripEvent,
    val toState: TripState,
    val sideEffects: List<TripSideEffect>,
    val ignored: Boolean = false,
)
