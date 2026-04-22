package com.nearwake.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.nearwake.core.datastore.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES_FILE) },
    )

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        dataStore: DataStore<Preferences>,
    ): UserPreferencesDataStore = UserPreferencesDataStore(dataStore)

    private const val USER_PREFERENCES_FILE = "user_preferences.preferences_pb"
}
