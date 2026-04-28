package com.nearwake.core.database.di

import com.nearwake.core.database.TripLifecycleStoreImpl
import com.nearwake.ports.persistence.TripLifecycleStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PersistenceBindingsModule {
    @Binds
    @Singleton
    abstract fun bindTripLifecycleStore(
        impl: TripLifecycleStoreImpl,
    ): TripLifecycleStore
}
