package com.nearwake.feature.tripsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.domain.trip.model.AlertIntensity

@Composable
fun TripSetupScreen(
    onStartTrip: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TripSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Trip setup",
        subtitle = "Tune the lead time once, then let the monitoring engine do the quiet work.",
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }

        NearWakeCard {
            Text("To: ${state.destinationName}", style = MaterialTheme.typography.titleLarge)
            if (state.destinationAddress.isNotBlank()) {
                Text(state.destinationAddress, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("ETA: ${state.etaLabel}", color = MaterialTheme.colorScheme.tertiary)
            Text(state.routeSummary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        NearWakeCard {
            Text("Alert me", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(5, 10, 15, 0).forEach { minutes ->
                    val label = if (minutes == 0) "Nearby" else "${minutes}m"
                    FilterChip(
                        selected = state.alertLeadMinutes == minutes,
                        onClick = { viewModel.selectLeadMinutes(minutes) },
                        label = { Text(label) },
                    )
                }
            }
        }

        NearWakeCard {
            Text("Alert intensity", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertIntensity.entries.forEach { intensity ->
                    FilterChip(
                        selected = state.alertIntensity == intensity,
                        onClick = { viewModel.selectIntensity(intensity) },
                        label = { Text(intensity.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
        }

        NearWakeCard {
            Text("Background monitoring", style = MaterialTheme.typography.titleLarge)
            Switch(
                checked = state.backgroundMonitoringEnabled,
                onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
            )
        }

        NearWakePrimaryButton(
            text = "Start trip",
            onClick = { viewModel.startTrip(onStartTrip) },
            enabled = state.canStart,
        )
    }
}
