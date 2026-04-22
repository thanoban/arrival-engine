package com.nearwake.data.alerts.di

import androidx.work.Configuration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    @Provides
    fun provideWorkerConfiguration(): Configuration =
        Configuration.Builder().build()
}
