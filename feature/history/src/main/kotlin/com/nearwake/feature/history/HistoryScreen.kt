package com.nearwake.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.SurfaceCard

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
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        NearWakeSectionHeader(text = "Recent trips")
        if (state.trips.isEmpty()) {
            SurfaceCard {
                Text("No trips yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Start a trip from the destination picker and it will appear here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.trips.forEach { trip ->
            SurfaceCard(modifier = androidx.compose.ui.Modifier.clickable { onTripSelected(trip.tripId) }) {
                NearWakeStateChip(
                    label = trip.statusLabel,
                    state = when (trip.statusLabel) {
                        "Completed" -> NearWakeChipState.Safe
                        "Monitoring now" -> NearWakeChipState.Monitoring
                        else -> NearWakeChipState.Approaching
                    },
                )
                Text(trip.destinationName, style = MaterialTheme.typography.titleMedium)
                Text(trip.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
