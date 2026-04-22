package com.nearwake.feature.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun TripSummaryScreen(
    onBack: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NearWakeScaffold(
        title = state.destinationName,
        subtitle = state.destinationAddress.ifBlank { "Trip summary" },
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }

        NearWakeCard {
            Text("Status", style = MaterialTheme.typography.titleMedium)
            Text(state.statusLabel)
        }

        NearWakeCard {
            Text("Started", style = MaterialTheme.typography.titleMedium)
            Text(state.startedLabel.ifBlank { "Pending" })
            Text(state.monitoringLabel, color = MaterialTheme.colorScheme.tertiary)
        }

        NearWakeCard {
            Text("Alert setup", style = MaterialTheme.typography.titleMedium)
            Text(state.alertLeadLabel.ifBlank { "Lead time unavailable" })
            Text(state.alertIntensityLabel.ifBlank { "Alert intensity unavailable" })
        }
    }
}
