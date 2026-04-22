package com.nearwake.core.testing

import com.nearwake.domain.location.model.GeofenceSpec
import com.nearwake.domain.location.repository.GeofenceRepository

class FakeGeofenceClient : GeofenceRepository {
    private val registeredGeofences = linkedMapOf<String, GeofenceSpec>()

    val removeRequests = mutableListOf<List<String>>()
    var removeAllCallCount: Int = 0
        private set

    override suspend fun registerGeofences(specs: List<GeofenceSpec>) {
        specs.forEach { spec ->
            registeredGeofences[spec.id] = spec
        }
    }

    override suspend fun removeGeofences(ids: List<String>) {
        removeRequests += ids
        ids.forEach(registeredGeofences::remove)
    }

    override suspend fun removeAllGeofences() {
        removeAllCallCount += 1
        registeredGeofences.clear()
    }

    fun snapshot(): List<GeofenceSpec> = registeredGeofences.values.toList()
}
