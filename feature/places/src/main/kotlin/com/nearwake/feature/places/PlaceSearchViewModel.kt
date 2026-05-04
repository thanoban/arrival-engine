package com.nearwake.feature.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.entity.SavedPlaceEntity
import com.nearwake.domain.location.model.PlaceSearchResult
import com.nearwake.domain.location.repository.PlaceSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class PlaceSearchResultUiModel(
    val id: String,
    val name: String,
    val address: String,
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
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PlaceSearchViewModel @Inject constructor(
    private val savedPlaceDao: SavedPlaceDao,
    private val placeSearchRepository: PlaceSearchRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val mutableState = MutableStateFlow(PlaceSearchUiState())
    val state: StateFlow<PlaceSearchUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            savedPlaceDao.observeSavedPlaces().collect { savedPlaces ->
                mutableState.update { state ->
                    state.copy(savedPlaces = savedPlaces.map { place -> place.toUiModel() })
                }
            }
        }
        viewModelScope.launch {
            query.collectLatest { currentQuery ->
                search(currentQuery)
            }
        }
    }

    fun updateQuery(value: String) {
        query.value = value
        mutableState.update { state ->
            state.copy(query = value, errorMessage = null)
        }
    }

    fun selectResult(result: PlaceSearchResultUiModel, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val resolved = placeSearchRepository.resolvePlace(result.id)
                .onFailure {
                    mutableState.update { state ->
                        state.copy(errorMessage = "Could not load that place. Try another result.")
                    }
                }
                .getOrNull()
                ?: return@launch
            savedPlaceDao.upsertSavedPlace(
                SavedPlaceEntity(
                    id = resolved.id,
                    name = resolved.name,
                    address = resolved.address,
                    lat = resolved.latLng.lat,
                    lng = resolved.latLng.lng,
                    placeId = resolved.id,
                    lastUsedAt = Clock.System.now(),
                ),
            )
            onSaved(resolved.id)
        }
    }

    fun selectSavedPlace(placeId: String, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            savedPlaceDao.getSavedPlaceById(placeId)?.let { place ->
                savedPlaceDao.upsertSavedPlace(place.copy(lastUsedAt = Clock.System.now()))
            }
            onSaved(placeId)
        }
    }

    private suspend fun search(currentQuery: String) {
        if (currentQuery.trim().length < 2) {
            mutableState.update { state ->
                state.copy(results = emptyList(), isSearching = false, errorMessage = null)
            }
            return
        }
        mutableState.update { state ->
            state.copy(isSearching = true, errorMessage = null)
        }
        placeSearchRepository.searchPlaces(currentQuery)
            .onSuccess { results ->
                mutableState.update { state ->
                    state.copy(
                        results = results.map { result -> result.toUiModel() },
                        isSearching = false,
                        errorMessage = null,
                    )
                }
            }
            .onFailure {
                mutableState.update { state ->
                    state.copy(
                        results = emptyList(),
                        isSearching = false,
                        errorMessage = "Place search is unavailable right now.",
                    )
                }
            }
    }
}

private fun SavedPlaceEntity.toUiModel(): SavedPlaceUiModel =
    SavedPlaceUiModel(
        id = id,
        name = name,
        address = address,
        lastUsedLabel = lastUsedAt?.let { "Used ${it.toString().take(10)}" } ?: "Saved for later",
    )

private fun PlaceSearchResult.toUiModel(): PlaceSearchResultUiModel =
    PlaceSearchResultUiModel(
        id = id,
        name = name,
        address = address,
    )
