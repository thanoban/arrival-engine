package com.nearwake.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun SavedPlacesScreen(
    onSelectPlace: (String) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: PlaceSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NearWakeScaffold(
        title = "Saved places",
        subtitle = "Places you have used recently are ready for one-tap arming.",
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        SavedPlacesSection(
            places = state.savedPlaces,
            onSelectPlace = { placeId -> viewModel.selectSavedPlace(placeId, onSelectPlace) },
        )
    }
}

@Composable
internal fun SavedPlacesSection(
    places: List<SavedPlaceUiModel>,
    onSelectPlace: (String) -> Unit,
) {
    if (places.isEmpty()) {
        SurfaceCard {
            Text("No saved places yet.", style = MaterialTheme.typography.titleMedium)
            Text(
                "Search for a stop once and it will appear here for faster setup.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saved places", style = MaterialTheme.typography.titleLarge)
        places.forEach { place ->
            SurfaceCard {
                NearWakeStateChip(
                    label = place.lastUsedLabel,
                    state = NearWakeChipState.Monitoring,
                )
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                Text(place.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                NearWakeSecondaryButton(text = "Use saved place", onClick = { onSelectPlace(place.id) })
            }
        }
    }
}
