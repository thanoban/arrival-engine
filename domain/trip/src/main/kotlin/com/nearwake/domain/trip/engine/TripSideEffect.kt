package com.nearwake.domain.trip.engine

sealed interface TripSideEffect {
    data object PersistSession : TripSideEffect

    data object RegisterGeofences : TripSideEffect

    data object RegisterActivityTransitions : TripSideEffect

    data object StartBalancedTracking : TripSideEffect

    data object StartPreciseBurst : TripSideEffect

    data object PromptForMovement : TripSideEffect

    data object ApplyOfflineBias : TripSideEffect

    data object ClearOfflineBias : TripSideEffect

    data object StopLocationTracking : TripSideEffect

    data object FireArrivalAlert : TripSideEffect

    data object LogTripCompletion : TripSideEffect

    data object StartRecoveryChecks : TripSideEffect

    data object RestartMonitoring : TripSideEffect

    data object CleanupMonitoring : TripSideEffect

    data object RestorePersistedMonitoring : TripSideEffect
}
