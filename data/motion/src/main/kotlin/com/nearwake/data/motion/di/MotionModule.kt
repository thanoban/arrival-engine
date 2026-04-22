package com.nearwake.data.motion.di

import com.nearwake.data.motion.MotionRepositoryImpl
import com.nearwake.domain.location.repository.MotionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MotionModule {
    @Binds
    abstract fun bindMotionRepository(
        impl: MotionRepositoryImpl,
    ): MotionRepository
}
