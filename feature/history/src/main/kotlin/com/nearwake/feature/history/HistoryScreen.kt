package com.nearwake.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onTripSelected: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Trip history",
        subtitle = "A quick view of the trips you completed or cancelled recently.",
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        if (state.trips.isEmpty()) {
            NearWakeCard {
                Text("No trips yet", style = MaterialTheme.typography.titleMedium)
                Text("Start a trip from the destination picker and it will appear here.")
            }
        }
        state.trips.forEach { trip ->
            NearWakeCard(modifier = androidx.compose.ui.Modifier.clickable { onTripSelected(trip.tripId) }) {
                Text(trip.destinationName, style = MaterialTheme.typography.titleMedium)
                Text(trip.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(trip.statusLabel, color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}
