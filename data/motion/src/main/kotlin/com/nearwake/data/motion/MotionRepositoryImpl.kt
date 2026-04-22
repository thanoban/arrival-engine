package com.nearwake.data.motion

import com.nearwake.domain.location.repository.MotionRepository
import javax.inject.Inject

class MotionRepositoryImpl @Inject constructor(
    private val activityRecognitionDataSource: ActivityRecognitionDataSource,
) : MotionRepository {
    override fun observeMotionState() =
        activityRecognitionDataSource.observeMotionState()
}
