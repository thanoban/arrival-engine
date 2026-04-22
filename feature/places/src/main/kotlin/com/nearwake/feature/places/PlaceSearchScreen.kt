package com.nearwake.feature.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun PlaceSearchScreen(
    onSelectPlace: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlaceSearchViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = "Pick a destination",
        subtitle = "Search now, save the place, and reuse it later from Home.",
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::updateQuery,
            label = { Text("Search") },
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.results.forEach { result ->
                NearWakeCard {
                    Text(result)
                    OutlinedButton(onClick = onSelectPlace) { Text("Use this place") }
                }
            }
        }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
