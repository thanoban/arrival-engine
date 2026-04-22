package com.nearwake.feature.settings

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
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.domain.trip.model.AlertIntensity

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDiagnostics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NearWakeScaffold(
        title = "Settings",
        subtitle = "Preferences for default alert timing, intensity, and diagnostics.",
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        OutlinedButton(onClick = onDiagnostics) { Text("Diagnostics") }

        NearWakeCard {
            Text("Default alert lead", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(5, 10, 15, 0).forEach { minutes ->
                    FilterChip(
                        selected = state.defaultAlertLeadMinutes == minutes,
                        onClick = { viewModel.updateLeadMinutes(minutes) },
                        label = {
                            Text(if (minutes == 0) "Nearby" else "${minutes}m")
                        },
                    )
                }
            }
        }

        NearWakeCard {
            Text("Default intensity", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertIntensity.entries.forEach { intensity ->
                    FilterChip(
                        selected = state.alertIntensity == intensity,
                        onClick = { viewModel.updateAlertIntensity(intensity) },
                        label = {
                            Text(intensity.name.lowercase().replaceFirstChar(Char::uppercase))
                        },
                    )
                }
            }
        }

        NearWakeCard {
            Text("Background monitoring", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = state.backgroundMonitoringEnabled,
                onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
            )
        }

        NearWakeCard {
            Text("Diagnostics", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = state.diagnosticsEnabled,
                onCheckedChange = viewModel::setDiagnosticsEnabled,
            )
        }
    }
}
