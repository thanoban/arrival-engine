package com.nearwake.data.routing

import com.google.common.truth.Truth.assertThat
import com.nearwake.core.database.dao.RouteSnapshotDao
import com.nearwake.core.database.entity.RouteSnapshotEntity
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.routing.model.RouteSnapshot
import com.nearwake.domain.routing.model.Stop
import com.nearwake.domain.routing.model.TransferPoint
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Test

class RoutingStubsTest {
    @Test
    fun `no op routing reports destination only mode`() = runTest {
        val result = NoOpRoutingDataSource().fetchRoute(
            origin = LatLng(6.9271, 79.8612),
            destination = LatLng(7.1808, 79.8841),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("destination-only mode")
    }

    @Test
    fun `google transit data source requires an api key`() = runTest {
        val dataSource = GoogleTransitDataSource(
            okHttpClient = clientReturning("{}"),
            json = Json { ignoreUnknownKeys = true },
            localRouteCache = LocalRouteCache(FakeRouteSnapshotDao()),
            apiKey = "",
            directionsBaseUrl = "https://maps.googleapis.com/".toHttpUrl(),
        )

        val result = dataSource.refreshEta(
            tripId = "trip-123",
            currentLocation = LatLng(6.9271, 79.8612),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("MAPS_API_KEY")
    }

    @Test
    fun `local route cache stores updates and removals`() = runTest {
        val cache = LocalRouteCache(FakeRouteSnapshotDao())
        val snapshot = RouteSnapshot(
            tripId = "trip-123",
            stops = listOf(
                Stop(name = "Central Station", lat = 6.9271, lng = 79.8612, order = 1),
                Stop(name = "Airport Terminal 2", lat = 7.1808, lng = 79.8841, order = 2),
            ),
            transfers = listOf(
                TransferPoint(
                    stop = Stop(name = "Central Station", lat = 6.9271, lng = 79.8612, order = 1),
                    lineName = "Blue Line",
                    arrivalMinutes = 12,
                ),
            ),
            totalDurationMinutes = 42,
            fetchedAt = Clock.System.now(),
            isStale = false,
        )

        assertThat(cache.get(snapshot.tripId)).isNull()

        cache.put(snapshot)
        assertThat(cache.get(snapshot.tripId)).isEqualTo(snapshot)

        cache.remove(snapshot.tripId)
        assertThat(cache.get(snapshot.tripId)).isNull()
    }

    @Test
    fun `google transit data source parses transit stops and transfers`() = runTest {
        val dataSource = GoogleTransitDataSource(
            okHttpClient = clientReturning(
                """
                {
                  "status": "OK",
                  "routes": [{
                    "legs": [{
                      "duration": { "value": 1920 },
                      "steps": [
                        { "travel_mode": "WALKING", "duration": { "value": 300 } },
                        {
                          "travel_mode": "TRANSIT",
                          "duration": { "value": 900 },
                          "transit_details": {
                            "departure_stop": {
                              "name": "Central Station",
                              "location": { "lat": 6.9271, "lng": 79.8612 }
                            },
                            "arrival_stop": {
                              "name": "Town Hall",
                              "location": { "lat": 6.9314, "lng": 79.8572 }
                            },
                            "line": { "short_name": "Blue Line" }
                          }
                        },
                        { "travel_mode": "WALKING", "duration": { "value": 120 } },
                        {
                          "travel_mode": "TRANSIT",
                          "duration": { "value": 600 },
                          "transit_details": {
                            "departure_stop": {
                              "name": "Town Hall",
                              "location": { "lat": 6.9314, "lng": 79.8572 }
                            },
                            "arrival_stop": {
                              "name": "Airport Terminal",
                              "location": { "lat": 7.1808, "lng": 79.8841 }
                            },
                            "line": { "short_name": "Green Line" }
                          }
                        }
                      ]
                    }]
                  }]
                }
                """.trimIndent(),
            ),
            json = Json { ignoreUnknownKeys = true },
            localRouteCache = LocalRouteCache(FakeRouteSnapshotDao()),
            apiKey = "test-key",
            directionsBaseUrl = "https://maps.googleapis.com/".toHttpUrl(),
        )

        val result = dataSource.fetchRoute(
            origin = LatLng(6.9271, 79.8612),
            destination = LatLng(7.1808, 79.8841),
        ).getOrThrow()

        assertThat(result.totalDurationMinutes).isEqualTo(32)
        assertThat(result.stops.map(Stop::name))
            .containsExactly("Central Station", "Town Hall", "Airport Terminal")
            .inOrder()
        assertThat(result.transfers).hasSize(1)
        assertThat(result.transfers.first().lineName).isEqualTo("Green Line")
        assertThat(result.transfers.first().stop.name).isEqualTo("Town Hall")
        assertThat(result.transfers.first().arrivalMinutes).isEqualTo(20)
    }

    @Test
    fun `google transit data source refreshes eta from cached destination`() = runTest {
        val cache = LocalRouteCache(FakeRouteSnapshotDao())
        val capturedUrls = mutableListOf<String>()
        val dataSource = GoogleTransitDataSource(
            okHttpClient = clientReturning(
                """
                {
                  "status": "OK",
                  "routes": [{
                    "legs": [{
                      "duration": { "value": 780 },
                      "steps": []
                    }]
                  }]
                }
                """.trimIndent(),
                capturedUrls = capturedUrls,
            ),
            json = Json { ignoreUnknownKeys = true },
            localRouteCache = cache,
            apiKey = "test-key",
            directionsBaseUrl = "https://maps.googleapis.com/".toHttpUrl(),
        )
        cache.put(
            RouteSnapshot(
                tripId = "trip-123",
                stops = listOf(
                    Stop(name = "Central Station", lat = 6.9271, lng = 79.8612, order = 1),
                    Stop(name = "Airport Terminal", lat = 7.1808, lng = 79.8841, order = 2),
                ),
                transfers = emptyList(),
                totalDurationMinutes = 42,
                fetchedAt = Clock.System.now(),
                isStale = false,
            ),
        )

        val eta = dataSource.refreshEta(
            tripId = "trip-123",
            currentLocation = LatLng(6.9500, 79.8700),
        ).getOrThrow()

        assertThat(eta).isEqualTo(13)
        assertThat(capturedUrls.single()).contains("mode=transit")
        assertThat(capturedUrls.single()).contains("key=test-key")
    }

    @Test
    fun `google transit data source fails when refresh eta has no cached route`() = runTest {
        val dataSource = GoogleTransitDataSource(
            okHttpClient = clientReturning("{}"),
            json = Json { ignoreUnknownKeys = true },
            localRouteCache = LocalRouteCache(FakeRouteSnapshotDao()),
            apiKey = "test-key",
            directionsBaseUrl = "https://maps.googleapis.com/".toHttpUrl(),
        )

        val result = dataSource.refreshEta(
            tripId = "missing-trip",
            currentLocation = LatLng(6.9271, 79.8612),
        )

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("No cached route snapshot")
    }

    private fun clientReturning(
        body: String,
        capturedUrls: MutableList<String> = mutableListOf(),
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            capturedUrls += chain.request().url.toString()
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody(contentType = null))
                .build()
        }
        .build()

    private class FakeRouteSnapshotDao : RouteSnapshotDao {
        private val snapshots = mutableMapOf<String, RouteSnapshotEntity>()

        override suspend fun getRouteSnapshotByTripId(tripId: String): RouteSnapshotEntity? =
            snapshots[tripId]

        override suspend fun upsertRouteSnapshot(snapshot: RouteSnapshotEntity) {
            snapshots[snapshot.tripId] = snapshot
        }

        override suspend fun deleteRouteSnapshotByTripId(tripId: String) {
            snapshots.remove(tripId)
        }
    }
}
