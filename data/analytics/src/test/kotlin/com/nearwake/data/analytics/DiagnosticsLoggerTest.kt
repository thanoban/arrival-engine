package com.nearwake.data.analytics

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.entity.DiagnosticsEventEntity
import com.nearwake.core.datastore.UserPreferencesDataStore
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DiagnosticsLoggerTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `log skips writes when diagnostics logging is disabled`() = runTest {
        val dao = FakeDiagnosticsEventDao()
        val preferences = userPreferencesDataStore()
        val logger = DiagnosticsLogger(
            userPreferencesDataStore = preferences,
            diagnosticsEventDao = dao,
        )

        logger.log(eventType = "alert_fired")

        assertTrue(dao.events.isEmpty())
    }

    @Test
    fun `log writes events when diagnostics logging is enabled`() = runTest {
        val dao = FakeDiagnosticsEventDao()
        val preferences = userPreferencesDataStore().also {
            it.setDiagnosticsEnabled(true)
        }
        val logger = DiagnosticsLogger(
            userPreferencesDataStore = preferences,
            diagnosticsEventDao = dao,
        )

        logger.log(
            eventType = "alert_fired",
            tripId = "trip-123",
            payload = buildJsonObject {},
        )

        assertEquals(1, dao.events.size)
        assertEquals("alert_fired", dao.events.single().eventType)
        assertEquals("trip-123", dao.events.single().tripId)
    }

    private fun TestScope.userPreferencesDataStore(): UserPreferencesDataStore =
        UserPreferencesDataStore(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(tempDir, "user_preferences.preferences_pb") },
            ),
        )
}

private class FakeDiagnosticsEventDao : DiagnosticsEventDao {
    val events = mutableListOf<DiagnosticsEventEntity>()

    override fun observeRecentEvents(limit: Int): Flow<List<DiagnosticsEventEntity>> = flowOf(emptyList())

    override fun observeEventsForTrip(tripId: String): Flow<List<DiagnosticsEventEntity>> = flowOf(emptyList())

    override suspend fun upsertEvent(event: DiagnosticsEventEntity) {
        events += event
    }

    override suspend fun clearAll() {
        events.clear()
    }
}
