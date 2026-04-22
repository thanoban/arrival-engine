package com.nearwake.data.location.receivers

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object GeofenceEventBus {
    private val mutableEvents = MutableSharedFlow<GeofenceTransitionEvent>(
        extraBufferCapacity = 16,
    )

    val events: SharedFlow<GeofenceTransitionEvent> = mutableEvents.asSharedFlow()

    fun emit(event: GeofenceTransitionEvent) {
        mutableEvents.tryEmit(event)
    }
}
