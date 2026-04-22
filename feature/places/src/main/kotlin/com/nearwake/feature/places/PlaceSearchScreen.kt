package com.nearwake.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun PlaceSearchScreen(
    onSelectPlace: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: PlaceSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NearWakeScaffold(
        title = "Pick a destination",
        subtitle = "Search now, save the place, and reuse it later from Home.",
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            label = { Text("Search") },
        )

        if (state.savedPlaces.isNotEmpty()) {
            SavedPlacesSection(
                places = state.savedPlaces,
                onSelectPlace = { placeId -> viewModel.selectSavedPlace(placeId, onSelectPlace) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.results.forEach { result ->
                NearWakeCard {
                    Text(result.name, style = MaterialTheme.typography.titleLarge)
                    Text(result.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { viewModel.selectResult(result, onSelectPlace) }) {
                        Text("Use this place")
                    }
                }
            }
            if (state.results.isEmpty()) {
                NearWakeCard {
                    Text("No places matched that search yet.")
                    Text(
                        "Try a broader station, airport, or landmark name.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
