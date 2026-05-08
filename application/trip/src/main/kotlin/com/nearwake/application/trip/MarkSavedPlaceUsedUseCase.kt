package com.nearwake.application.trip

import com.nearwake.ports.persistence.TripLifecycleStore
import javax.inject.Inject
import kotlinx.datetime.Clock

class MarkSavedPlaceUsedUseCase @Inject constructor(
    private val tripLifecycleStore: TripLifecycleStore,
) {
    suspend operator fun invoke(placeId: String) {
        tripLifecycleStore.markPlaceUsed(
            placeId = placeId,
            usedAt = Clock.System.now(),
        )
    }
}
