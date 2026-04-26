package com.nearwake.domain.location.repository

import com.nearwake.domain.location.model.PlaceSearchResult
import com.nearwake.domain.location.model.ResolvedPlace

interface PlaceSearchRepository {
    suspend fun searchPlaces(query: String): Result<List<PlaceSearchResult>>

    suspend fun resolvePlace(placeId: String): Result<ResolvedPlace>
}
