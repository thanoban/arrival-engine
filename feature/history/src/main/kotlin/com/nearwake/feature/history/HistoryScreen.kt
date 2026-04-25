package com.nearwake.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onTripSelected: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProvideNearWakeStateAccent(NearWakeColors.MonitoringBase) {
        NearWakeScaffold(
            title = "Trip history",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            NearWakeSectionHeader(text = "All trips")
            if (state.trips.isEmpty()) {
                SurfaceCard {
                    Text(
                        text = "No trips yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Start a trip from the destination picker and it will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                state.trips.forEach { trip ->
                    SurfaceCard(modifier = Modifier.clickable { onTripSelected(trip.tripId) }) {
                        NearWakeStateChip(
                            label = trip.statusLabel,
                            state = when (trip.statusLabel) {
                                "Completed" -> NearWakeChipState.Safe
                                "Monitoring now" -> NearWakeChipState.Monitoring
                                else -> NearWakeChipState.Approaching
                            },
                        )
                        Text(
                            text = trip.destinationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = trip.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
