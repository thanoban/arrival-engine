package com.nearwake.feature.diagnostics

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
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Diagnostics",
        subtitle = "QA visibility into current state, registered geofences, and recent engine events.",
    ) {
        NearWakeCard {
            Text("Current trip state: ${state.stateLabel}")
        }
        NearWakeCard {
            Text("Registered geofences")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.registeredGeofences.isEmpty()) {
                    Text("No geofences registered", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.registeredGeofences.forEach { Text(it) }
                }
            }
        }
        NearWakeCard {
            Text("Recent events")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.recentEvents.isEmpty()) {
                    Text("No diagnostics events recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.recentEvents.forEach { Text(it) }
                }
            }
        }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
