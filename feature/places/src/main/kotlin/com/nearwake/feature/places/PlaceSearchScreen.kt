package com.nearwake.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.SurfaceCard

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
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            label = { Text("Search") },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        )

        if (state.savedPlaces.isNotEmpty()) {
            NearWakeSectionHeader(text = "Saved places")
            SavedPlacesSection(
                places = state.savedPlaces,
                onSelectPlace = { placeId -> viewModel.selectSavedPlace(placeId, onSelectPlace) },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NearWakeSectionHeader(text = "Search results")
            state.results.forEach { result ->
                SurfaceCard {
                    Text(result.name, style = MaterialTheme.typography.titleLarge)
                    Text(result.address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    NearWakeSecondaryButton(
                        text = "Use this place",
                        onClick = { viewModel.selectResult(result, onSelectPlace) },
                    )
                }
            }
            if (state.results.isEmpty()) {
                SurfaceCard {
                    Text(if (state.query.isBlank()) "Start typing to search places" else "No places matched that search yet.")
                    Text(
                        "Try a broader station, airport, or landmark name.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
