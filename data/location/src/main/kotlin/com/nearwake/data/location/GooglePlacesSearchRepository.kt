package com.nearwake.data.location

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.nearwake.domain.location.model.LatLng
import com.nearwake.domain.location.model.PlaceSearchResult
import com.nearwake.domain.location.model.ResolvedPlace
import com.nearwake.domain.location.repository.PlaceSearchRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class GooglePlacesSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlaceSearchRepository {
    override suspend fun searchPlaces(query: String): Result<List<PlaceSearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensurePlacesInitialized()
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query.trim())
                    .build()
                Places.createClient(context)
                    .findAutocompletePredictions(request)
                    .await()
                    .autocompletePredictions
                    .map { prediction ->
                        PlaceSearchResult(
                            id = prediction.placeId,
                            name = prediction.getPrimaryText(null).toString(),
                            address = prediction.getSecondaryText(null).toString().ifBlank {
                                prediction.getFullText(null).toString()
                            },
                        )
                    }
            }
        }

    @Suppress("DEPRECATION")
    override suspend fun resolvePlace(placeId: String): Result<ResolvedPlace> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensurePlacesInitialized()
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                )
                val place = Places.createClient(context)
                    .fetchPlace(FetchPlaceRequest.builder(placeId, fields).build())
                    .await()
                    .place
                val latLng = place.latLng
                    ?: throw IllegalStateException("Google Places returned no coordinates for placeId=$placeId.")
                ResolvedPlace(
                    id = place.id ?: placeId,
                    name = place.name.orEmpty().ifBlank { "Selected place" },
                    address = place.address.orEmpty(),
                    latLng = LatLng(lat = latLng.latitude, lng = latLng.longitude),
                )
            }
        }

    private fun ensurePlacesInitialized() {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
    }
}
