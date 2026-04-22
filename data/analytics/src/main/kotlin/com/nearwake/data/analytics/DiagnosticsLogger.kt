package com.nearwake.data.analytics

import com.nearwake.core.database.dao.DiagnosticsEventDao
import com.nearwake.core.database.entity.DiagnosticsEventEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject

@Singleton
class DiagnosticsLogger @Inject constructor(
    private val diagnosticsEventDao: DiagnosticsEventDao,
) {
    suspend fun log(
        eventType: String,
        tripId: String? = null,
        payload: JsonElement = buildJsonObject {},
    ) {
        diagnosticsEventDao.upsertEvent(
            DiagnosticsEventEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                eventType = eventType,
                payloadJson = Json.encodeToString(payload),
                recordedAt = Clock.System.now(),
            ),
        )
    }
}
