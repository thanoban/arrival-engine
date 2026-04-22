package com.nearwake.domain.location.repository

import com.nearwake.domain.location.model.MotionState
import kotlinx.coroutines.flow.Flow

interface MotionRepository {
    fun observeMotionState(): Flow<MotionState>
}
