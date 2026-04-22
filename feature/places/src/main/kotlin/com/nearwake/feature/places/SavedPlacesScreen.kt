package com.nearwake.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

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
    ) {
        SavedPlacesSection(
            places = state.savedPlaces,
            onSelectPlace = { placeId -> viewModel.selectSavedPlace(placeId, onSelectPlace) },
        )
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
internal fun SavedPlacesSection(
    places: List<SavedPlaceUiModel>,
    onSelectPlace: (String) -> Unit,
) {
    if (places.isEmpty()) {
        NearWakeCard {
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
            NearWakeCard {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                Text(place.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(place.lastUsedLabel, color = MaterialTheme.colorScheme.tertiary)
                OutlinedButton(onClick = { onSelectPlace(place.id) }) {
                    Text("Use saved place")
                }
            }
        }
    }
}
