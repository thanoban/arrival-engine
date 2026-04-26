package com.nearwake.data.patterns

import com.nearwake.core.database.dao.CommutePredictionDao
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import com.nearwake.core.database.entity.CommutePredictionEntity
import com.nearwake.domain.commute.CommutePrediction
import com.nearwake.domain.commute.CommutePredictionEngine
import com.nearwake.domain.commute.TripRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class CommutePredictionRepository @Inject constructor(
    private val tripDao: TripDao,
    private val savedPlaceDao: SavedPlaceDao,
    private val commutePredictionDao: CommutePredictionDao,
) {
    private val engine = CommutePredictionEngine()

    fun observePredictions(): Flow<List<CommutePrediction>> =
        commutePredictionDao.observePredictions().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getPredictions(): List<CommutePrediction> =
        commutePredictionDao.getPredictions().map { it.toDomain() }

    suspend fun refreshPredictions() {
        val trips = tripDao.getCompletedTrips()
        val places = savedPlaceDao.getAllSavedPlaces().associateBy { it.id }

        val records = trips.mapNotNull { trip ->
            val place = places[trip.destinationId] ?: return@mapNotNull null
            val duration = trip.completedAt?.let { completed ->
                ((completed.toEpochMilliseconds() - trip.createdAt.toEpochMilliseconds()) / 60_000).toInt()
            }
            TripRecord(
                id = trip.id,
                destinationId = trip.destinationId,
                destinationName = place.name,
                createdAt = trip.createdAt,
                durationMinutes = duration,
            )
        }

        val predictions = engine.predict(records)
        commutePredictionDao.upsertPredictions(
            predictions.map { it.toEntity() },
        )
    }
}

private fun CommutePrediction.toEntity(): CommutePredictionEntity =
    CommutePredictionEntity(
        id = id,
        originId = originId,
        destinationId = destinationId,
        destinationName = destinationName,
        daysOfWeekJson = Json.encodeToString(daysOfWeek.toList()),
        typicalDepartureHour = typicalDepartureHour,
        typicalDepartureMinute = typicalDepartureMinute,
        avgDurationMinutes = avgDurationMinutes,
        tripCount = tripCount,
        updatedAt = Clock.System.now(),
    )

private fun CommutePredictionEntity.toDomain(): CommutePrediction {
    val days = runCatching {
        Json.decodeFromString<List<Int>>(daysOfWeekJson).toSet()
    }.getOrDefault(emptySet())
    return CommutePrediction(
        id = id,
        originId = originId,
        destinationId = destinationId,
        destinationName = destinationName,
        daysOfWeek = days,
        typicalDepartureHour = typicalDepartureHour,
        typicalDepartureMinute = typicalDepartureMinute,
        avgDurationMinutes = avgDurationMinutes,
        tripCount = tripCount,
    )
}
