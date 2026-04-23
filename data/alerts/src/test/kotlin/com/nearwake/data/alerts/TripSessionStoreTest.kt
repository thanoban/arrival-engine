package com.nearwake.data.alerts

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.database.dao.TripSessionDao
import com.nearwake.core.database.entity.TripSessionEntity
import com.nearwake.core.testing.TripEngineTestFixtures
import com.nearwake.domain.trip.model.TripSession
import com.nearwake.domain.trip.model.TripState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TripSessionStoreTest {
    @Test
    fun `loadOrCreate returns persisted session when available`() = runTest {
        val dao = FakeTripSessionDao()
        val persistedSession = TripEngineTestFixtures.tripSession(
            tripId = "trip-123",
            state = TripState.MonitoringApproach,
            lastKnownLocation = TripEngineTestFixtures.latLng(),
        )
        dao.upsertTripSession(persistedSession.toEntity())
        val store = TripSessionStore(dao)

        val session = store.loadOrCreate("trip-123")

        assertThat(session).isEqualTo(persistedSession)
    }

    @Test
    fun `loadOrCreate seeds an armed session when persistence is empty`() = runTest {
        val dao = FakeTripSessionDao()
        val store = TripSessionStore(dao)

        val session = store.loadOrCreate("trip-456")

        assertThat(session.tripId).isEqualTo("trip-456")
        assertThat(session.state).isEqualTo(TripState.Armed)
        assertThat(dao.getTripSessionById("trip-456")).isNotNull()
    }

    @Test
    fun `save persists updated session fields`() = runTest {
        val dao = FakeTripSessionDao()
        val store = TripSessionStore(dao)
        val session = TripEngineTestFixtures.tripSession(
            tripId = "trip-789",
            state = TripState.MonitoringLowPower,
            lastKnownLocation = TripEngineTestFixtures.latLng(lat = 7.0, lng = 80.0),
            lastEtaMinutes = 18,
        )

        store.save(session)

        assertThat(dao.getTripSessionById("trip-789")).isEqualTo(session.toEntity())
    }

    private fun TripSession.toEntity(): TripSessionEntity =
        TripSessionEntity(
            tripId = tripId,
            state = state,
            monitoringMode = monitoringMode,
            lastKnownLat = lastKnownLat,
            lastKnownLng = lastKnownLng,
            lastEtaMinutes = lastEtaMinutes,
            confidence = confidence,
            geofenceIds = geofenceIds,
            updatedAt = updatedAt,
        )

    private class FakeTripSessionDao : TripSessionDao {
        private val sessions = linkedMapOf<String, TripSessionEntity>()

        override fun observeTripSessions(): Flow<List<TripSessionEntity>> =
            flowOf(sessions.values.toList())

        override fun observeTripSession(tripId: String): Flow<TripSessionEntity?> =
            flowOf(sessions[tripId])

        override suspend fun getTripSessionById(tripId: String): TripSessionEntity? =
            sessions[tripId]

        override suspend fun getActiveTripSession(): TripSessionEntity? =
            sessions.values.firstOrNull()

        override suspend fun upsertTripSession(session: TripSessionEntity) {
            sessions[session.tripId] = session
        }

        override suspend fun deleteTripSession(tripId: String) {
            sessions.remove(tripId)
        }
    }
}
