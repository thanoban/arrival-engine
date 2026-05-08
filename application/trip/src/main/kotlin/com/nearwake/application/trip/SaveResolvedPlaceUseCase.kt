package com.nearwake.application.trip

import com.nearwake.domain.location.model.ResolvedPlace
import com.nearwake.ports.persistence.SaveSavedPlaceCommand
import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock

class SaveResolvedPlaceUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke(place: ResolvedPlace) {
        tripLifecycleStore.saveSavedPlace(
            SaveSavedPlaceCommand(
                id = place.id,
                name = place.name,
                address = place.address,
                lat = place.latLng.lat,
                lng = place.latLng.lng,
                placeId = place.id,
                lastUsedAt = Clock.System.now(),
            ),
        )
    }
}
