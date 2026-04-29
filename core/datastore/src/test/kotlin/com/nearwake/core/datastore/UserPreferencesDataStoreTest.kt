package com.nearwake.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nearwake.core.datastore.model.ThemeMode
import com.nearwake.domain.trip.model.AlertTriggerMode
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
        assertEquals(AlertTriggerMode.TIME, preferences.defaultAlertTriggerMode)
        assertEquals(500, preferences.defaultAlertDistanceMeters)
        assertEquals(true, preferences.departureRemindersEnabled)
    }

    @Test
    fun `updateThemeMode persists selected theme mode`() = runTest {
        val store = userPreferencesDataStore()

        store.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, store.preferences.first().themeMode)
    }

    @Test
    fun `setDepartureRemindersEnabled persists departure reminder preference`() = runTest {
        val store = userPreferencesDataStore()

        store.setDepartureRemindersEnabled(false)

        assertEquals(false, store.preferences.first().departureRemindersEnabled)
    }

    @Test
    fun `alert trigger mode and distance persist`() = runTest {
        val store = userPreferencesDataStore()

        store.updateDefaultAlertTriggerMode(AlertTriggerMode.BOTH)
        store.updateDefaultAlertDistanceMeters(1500)

        val preferences = store.preferences.first()
        assertEquals(AlertTriggerMode.BOTH, preferences.defaultAlertTriggerMode)
        assertEquals(1500, preferences.defaultAlertDistanceMeters)
    }

    private fun TestScope.userPreferencesDataStore(): UserPreferencesDataStore =
        UserPreferencesDataStore(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tempDir, "user_preferences.preferences_pb") },
            ),
        )
}
