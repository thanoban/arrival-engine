package com.nearwake.feature.diagnostics

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
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Diagnostics",
        subtitle = "QA visibility into current state, registered geofences, and recent engine events.",
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        SurfaceCard {
            NearWakeSectionHeader(text = "Services")
            NearWakeStateChip(
                label = state.stateLabel.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                state = if (state.stateLabel == "No active trip") NearWakeChipState.Neutral else NearWakeChipState.Monitoring,
            )
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Location")
            Text("Registered geofences", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.registeredGeofences.isEmpty()) {
                    Text("No geofences registered", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.registeredGeofences.forEach { Text(it) }
                }
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Last session")
            Text("Recent events", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.recentEvents.isEmpty()) {
                    Text("No diagnostics events recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.recentEvents.forEach { Text(it) }
                }
            }
        }
    }
}
