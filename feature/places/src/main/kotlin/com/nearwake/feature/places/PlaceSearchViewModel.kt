package com.nearwake.feature.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.application.trip.MarkSavedPlaceUsedUseCase
import com.nearwake.application.trip.ObservePlaceSearchUseCase
import com.nearwake.application.trip.SaveResolvedPlaceUseCase
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
    observePlaceSearch: ObservePlaceSearchUseCase,
    private val markSavedPlaceUsed: MarkSavedPlaceUsedUseCase,
    private val saveResolvedPlace: SaveResolvedPlaceUseCase,
    private val placeSearchRepository: PlaceSearchRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val mutableState = MutableStateFlow(PlaceSearchUiState())
    val state: StateFlow<PlaceSearchUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            observePlaceSearch().collect { savedPlaces ->
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
            saveResolvedPlace(resolved)
            onSaved(resolved.id)
        }
    }

    fun selectSavedPlace(placeId: String, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            markSavedPlaceUsed(placeId)
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

private fun com.nearwake.application.trip.SavedPlacePresentation.toUiModel(): SavedPlaceUiModel =
    SavedPlaceUiModel(
        id = id,
        name = name,
        address = address,
        lastUsedLabel = lastUsedLabel,
    )

private fun PlaceSearchResult.toUiModel(): PlaceSearchResultUiModel =
    PlaceSearchResultUiModel(
        id = id,
        name = name,
        address = address,
    )
