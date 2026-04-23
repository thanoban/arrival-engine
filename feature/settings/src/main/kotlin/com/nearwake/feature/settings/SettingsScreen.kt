package com.nearwake.feature.settings

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeSelectableChip
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.domain.trip.model.AlertIntensity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onDiagnostics: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    NearWakeScaffold(
        title = "Settings",
        subtitle = "Preferences for default alert timing, intensity, and diagnostics.",
        topBarActions = {
            NearWakeSecondaryButton(text = "Diagnostics", onClick = onDiagnostics)
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        SurfaceCard {
            NearWakeSectionHeader(text = "Alerts")
            Text("Default alert lead", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                listOf(5, 10, 15, 0).forEach { minutes ->
                    NearWakeSelectableChip(
                        selected = state.defaultAlertLeadMinutes == minutes,
                        onClick = { viewModel.updateLeadMinutes(minutes) },
                        label = if (minutes == 0) "Nearby" else "${minutes} min",
                    )
                }
            }
            Text("Default intensity", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                AlertIntensity.entries.forEach { intensity ->
                    NearWakeSelectableChip(
                        selected = state.alertIntensity == intensity,
                        onClick = { viewModel.updateAlertIntensity(intensity) },
                        label = intensity.name.lowercase().replaceFirstChar(Char::uppercase),
                    )
                }
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Monitoring")
            SettingSwitchRow(
                title = "Background monitoring",
                subtitle = "Keeps trip monitoring alive while the phone is locked.",
                checked = state.backgroundMonitoringEnabled,
                onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
            )
            SettingSwitchRow(
                title = "Diagnostics",
                subtitle = "Stores recent engine events for troubleshooting.",
                checked = state.diagnosticsEnabled,
                onCheckedChange = viewModel::setDiagnosticsEnabled,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
