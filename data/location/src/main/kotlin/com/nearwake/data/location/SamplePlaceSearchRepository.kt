package com.nearwake.data.location

import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.model.PlaceSearchResult
import com.nearwake.domain.location.model.ResolvedPlace
import com.nearwake.domain.location.repository.PlaceSearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SamplePlaceSearchRepository @Inject constructor() : PlaceSearchRepository {
    override suspend fun searchPlaces(query: String): Result<List<PlaceSearchResult>> =
        Result.success(samplePlaces.filterMatches(query))

    override suspend fun resolvePlace(placeId: String): Result<ResolvedPlace> =
        samplePlaces.firstOrNull { place -> place.id == placeId }
            ?.let { place -> Result.success(place) }
            ?: Result.failure(IllegalArgumentException("No sample place exists for placeId=$placeId."))

    companion object {
        private val samplePlaces = listOf(
            ResolvedPlace(
                id = "central-station",
                name = "Central Station",
                address = "1 Station Plaza",
                latLng = LatLng(lat = 6.9271, lng = 79.8612),
            ),
            ResolvedPlace(
                id = "airport-terminal-2",
                name = "Airport Terminal 2",
                address = "Bandaranaike International Airport",
                latLng = LatLng(lat = 7.1808, lng = 79.8841),
            ),
            ResolvedPlace(
                id = "university-gate",
                name = "University Gate",
                address = "Engineering Faculty Main Entrance",
                latLng = LatLng(lat = 6.9068, lng = 79.8706),
            ),
        )
    }
}

private fun List<ResolvedPlace>.filterMatches(query: String): List<PlaceSearchResult> {
    val normalized = query.trim().lowercase()
    return filter { place ->
        normalized.isBlank() ||
            place.name.lowercase().contains(normalized) ||
            place.address.lowercase().contains(normalized)
    }.map { place ->
        PlaceSearchResult(
            id = place.id,
            name = place.name,
            address = place.address,
        )
    }
}
