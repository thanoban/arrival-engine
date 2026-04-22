package com.nearwake.feature.places

import androidx.lifecycle.ViewModel
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PlaceSearchResultUiModel(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
)

data class SavedPlaceUiModel(
    val id: String,
    val name: String,
    val address: String,
    val lastUsedLabel: String,
)

data class PlaceSearchUiState(
    val query: String = "",
    val results: List<PlaceSearchResultUiModel> = emptyList(),
    val savedPlaces: List<SavedPlaceUiModel> = emptyList(),
)

@HiltViewModel
class PlaceSearchViewModel @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val query = MutableStateFlow("")
    private val mutableState = MutableStateFlow(
        PlaceSearchUiState(results = sampleResults),
    )
    val state: StateFlow<PlaceSearchUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(query, savedPlaceDao.observeSavedPlaces()) { currentQuery, savedPlaces ->
                PlaceSearchUiState(
                    query = currentQuery,
                    results = sampleResults.filterMatches(currentQuery),
                    savedPlaces = savedPlaces.map { place ->
                        SavedPlaceUiModel(
                            id = place.id,
                            name = place.name,
                            address = place.address,
                            lastUsedLabel = place.lastUsedAt?.let { "Used ${it.toString().take(10)}" } ?: "Saved for later",
                        )
                    },
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
    }

    fun selectResult(result: PlaceSearchResultUiModel, onSaved: (String) -> Unit) {
        scope.launch {
            savedPlaceDao.upsertSavedPlace(
                SavedPlaceEntity(
                    id = result.id,
                    name = result.name,
                    address = result.address,
                    lat = result.lat,
                    lng = result.lng,
                    lastUsedAt = Clock.System.now(),
                ),
            )
            onSaved(result.id)
        }
    }

    fun selectSavedPlace(placeId: String, onSaved: (String) -> Unit) {
        scope.launch {
            savedPlaceDao.getSavedPlaceById(placeId)?.let { place ->
                savedPlaceDao.upsertSavedPlace(place.copy(lastUsedAt = Clock.System.now()))
            }
            onSaved(placeId)
        }
    }

    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }

    companion object {
        private val sampleResults = listOf(
            PlaceSearchResultUiModel(
                id = "central-station",
                name = "Central Station",
                address = "1 Station Plaza",
                lat = 6.9271,
                lng = 79.8612,
            ),
            PlaceSearchResultUiModel(
                id = "airport-terminal-2",
                name = "Airport Terminal 2",
                address = "Bandaranaike International Airport",
                lat = 7.1808,
                lng = 79.8841,
            ),
            PlaceSearchResultUiModel(
                id = "university-gate",
                name = "University Gate",
                address = "Engineering Faculty Main Entrance",
                lat = 6.9068,
                lng = 79.8706,
            ),
        )
    }
}

private fun List<PlaceSearchResultUiModel>.filterMatches(query: String): List<PlaceSearchResultUiModel> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return this
    return filter { result ->
        result.name.lowercase().contains(normalized) ||
            result.address.lowercase().contains(normalized)
    }
}
