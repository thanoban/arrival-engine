package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

data class SavedPlacePresentation(
    val id: String,
    val name: String,
    val address: String,
    val lastUsedLabel: String,
)

class ObservePlaceSearchUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    operator fun invoke(): Flow<List<SavedPlacePresentation>> =
        tripLifecycleStore.observeSavedPlaces().mapLatest { places ->
            places.map { place ->
                SavedPlacePresentation(
                    id = place.id,
                    name = place.name,
                    address = place.address,
                    lastUsedLabel = place.lastUsedAt?.let { usedAt ->
                        "Used ${usedAt.toString().take(10)}"
                    } ?: "Saved for later",
                )
            }
        }
}
