package com.nearwake.data.motion.receivers

import com.nearwake.domain.location.model.MotionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ActivityTransitionEventBus {
    private val mutableEvents = MutableSharedFlow<MotionState>(
        extraBufferCapacity = 16,
    )

    val events: SharedFlow<MotionState> = mutableEvents.asSharedFlow()

    fun emit(event: MotionState) {
        mutableEvents.tryEmit(event)
    }
}
