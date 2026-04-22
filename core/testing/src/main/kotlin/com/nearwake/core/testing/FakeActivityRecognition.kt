package com.nearwake.core.testing

import com.nearwake.domain.location.model.MotionState
import com.nearwake.domain.location.repository.MotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeActivityRecognition(
    initialState: MotionState = TripEngineTestFixtures.motionState(),
) : MotionRepository {
    private val state = MutableStateFlow(initialState)

    override fun observeMotionState(): Flow<MotionState> = state

    suspend fun emit(motionState: MotionState) {
        state.emit(motionState)
    }

    fun tryEmit(motionState: MotionState) {
        state.value = motionState
    }
}
