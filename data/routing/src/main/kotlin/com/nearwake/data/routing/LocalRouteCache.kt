package com.nearwake.data.routing

import com.nearwake.domain.routing.model.RouteSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class LocalRouteCache @Inject constructor() {
    private val mutex = Mutex()
    private val snapshots = mutableMapOf<String, RouteSnapshot>()

    suspend fun get(tripId: String): RouteSnapshot? =
        mutex.withLock { snapshots[tripId] }

    suspend fun put(snapshot: RouteSnapshot) {
        mutex.withLock {
            snapshots[snapshot.tripId] = snapshot
        }
    }

    suspend fun remove(tripId: String) {
        mutex.withLock {
            snapshots.remove(tripId)
        }
    }
}
