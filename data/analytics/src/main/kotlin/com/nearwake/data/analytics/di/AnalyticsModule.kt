package com.nearwake.data.analytics.di

import com.nearwake.data.analytics.SentryNearWakeAnalytics
import com.nearwake.ports.analytics.NearWakeAnalytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun bindNearWakeAnalytics(
        impl: SentryNearWakeAnalytics,
    ): NearWakeAnalytics
}
