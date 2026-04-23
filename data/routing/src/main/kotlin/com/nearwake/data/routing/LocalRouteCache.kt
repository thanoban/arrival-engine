package com.nearwake.data.routing

import com.nearwake.core.database.dao.RouteSnapshotDao
import com.nearwake.core.database.entity.RouteSnapshotEntity
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class LocalRouteCache @Inject constructor(
    private val routeSnapshotDao: RouteSnapshotDao,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val stopListSerializer = ListSerializer(Stop.serializer())
    private val transferPointListSerializer = ListSerializer(TransferPoint.serializer())

    suspend fun get(tripId: String): RouteSnapshot? =
        routeSnapshotDao.getRouteSnapshotByTripId(tripId)?.toModel()

    suspend fun put(snapshot: RouteSnapshot) {
        routeSnapshotDao.upsertRouteSnapshot(snapshot.toEntity())
    }

    suspend fun remove(tripId: String) {
        routeSnapshotDao.deleteRouteSnapshotByTripId(tripId)
    }

    private fun RouteSnapshotEntity.toModel(): RouteSnapshot =
        RouteSnapshot(
            tripId = tripId,
            stops = json.decodeFromString(stopListSerializer, stopsJson),
            transfers = json.decodeFromString(transferPointListSerializer, transfersJson),
            totalDurationMinutes = totalDurationMinutes,
            fetchedAt = fetchedAt,
            isStale = isStale,
        )

    private fun RouteSnapshot.toEntity(): RouteSnapshotEntity =
        RouteSnapshotEntity(
            tripId = tripId,
            stopsJson = json.encodeToString(stopListSerializer, stops),
            transfersJson = json.encodeToString(transferPointListSerializer, transfers),
            totalDurationMinutes = totalDurationMinutes,
            fetchedAt = fetchedAt,
            isStale = isStale,
        )
}
