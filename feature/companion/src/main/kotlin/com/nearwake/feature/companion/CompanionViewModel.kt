package com.nearwake.feature.companion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearwake.core.database.dao.SavedPlaceDao
import com.nearwake.core.database.dao.TripDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CompanionUiState(
    val tripId: String = "",
    val title: String = "Arrival confirmation",
    val messagePreview: String = "I arrived safely.",
    val smsPreview: String = "I arrived safely.",
    val errorMessage: String? = null,
)

@HiltViewModel
class CompanionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tripDao: TripDao,
    savedPlaceDao: SavedPlaceDao,
) : ViewModel() {
    private val tripId = savedStateHandle.get<String>(TRIP_ID_ARG).orEmpty()
    private val mutableState = MutableStateFlow(CompanionUiState(tripId = tripId))
    val state: StateFlow<CompanionUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tripDao.observeTripById(tripId),
                savedPlaceDao.observeSavedPlaces(),
            ) { trip, places ->
                val place = trip?.destinationId?.let { destinationId ->
                    places.firstOrNull { it.id == destinationId }
                }
                val completedAt = trip?.completedAt?.toLocalDateTime(TimeZone.currentSystemDefault())
                val timeLabel = completedAt?.let { local ->
                    "%02d:%02d".format(local.hour, local.minute)
                } ?: "just now"
                val destinationName = place?.name ?: "my destination"
                val shareMessage = "I arrived at $destinationName at $timeLabel."
                CompanionUiState(
                    tripId = tripId,
                    title = "Arrival confirmed",
                    messagePreview = shareMessage,
                    smsPreview = shareMessage,
                )
            }.collect { uiState ->
                mutableState.value = uiState
            }
        }
    }

    companion object {
        const val TRIP_ID_ARG = "tripId"
    }
}
