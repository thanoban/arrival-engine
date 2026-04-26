package com.nearwake.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nearwake.core.datastore.model.ThemeMode
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class UserPreferencesDataStoreTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `theme mode defaults to system`() = runTest {
        val store = userPreferencesDataStore()

        val preferences = store.preferences.first()

        assertEquals(ThemeMode.SYSTEM, preferences.themeMode)
    }

    @Test
    fun `updateThemeMode persists selected theme mode`() = runTest {
        val store = userPreferencesDataStore()

        store.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, store.preferences.first().themeMode)
    }

    private fun TestScope.userPreferencesDataStore(): UserPreferencesDataStore =
        UserPreferencesDataStore(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tempDir, "user_preferences.preferences_pb") },
            ),
        )
}
