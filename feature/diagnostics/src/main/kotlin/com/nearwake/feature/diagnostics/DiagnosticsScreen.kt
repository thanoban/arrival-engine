package com.nearwake.feature.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = viewModel(),
) {
    val state = viewModel.state.value
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
                state.registeredGeofences.forEach { Text(it) }
            }
        }
        NearWakeCard {
            Text("Recent events")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recentEvents.forEach { Text(it) }
            }
        }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
