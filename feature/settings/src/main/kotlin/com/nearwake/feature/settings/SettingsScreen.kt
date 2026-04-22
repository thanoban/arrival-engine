package com.nearwake.feature.settings

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearwake.core.ui.NearWakeCard
import com.nearwake.core.ui.NearWakeScaffold

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDiagnostics: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state = viewModel.state.value
    NearWakeScaffold(
        title = "Settings",
        subtitle = "Preferences for default alert timing, intensity, and diagnostics.",
    ) {
        OutlinedButton(onClick = onBack) { Text("Back") }
        OutlinedButton(onClick = onDiagnostics) { Text("Diagnostics") }
        NearWakeCard {
            Text("Default alert lead: ${state.alertLeadSummary}")
            Text("Diagnostics enabled: ${state.diagnosticsEnabled}")
        }
    }
}
