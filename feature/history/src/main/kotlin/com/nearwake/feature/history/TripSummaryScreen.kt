package com.nearwake.feature.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeNumericText
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun TripSummaryScreen(
    onBack: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val accent = summaryAccent(state.statusLabel)

    ProvideNearWakeStateAccent(accent) {
        NearWakeScaffold(
            title = state.destinationName,
            subtitle = state.destinationAddress.ifBlank { null },
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            HeroCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${state.statusLabel}. ${state.startedLabel.ifBlank { "Trip start unavailable" }}. ${state.monitoringLabel}"
                },
                accent = accent,
            ) {
                NearWakeStateChip(
                    label = state.statusLabel,
                    state = summaryChipState(state.statusLabel),
                )
                Text(
                    text = state.startedLabel.ifBlank { "Trip start unavailable" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.monitoringLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ElevatedCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Route snapshot. ")
                        append(state.routeSummary)
                        if (state.etaLabel.isNotBlank()) {
                            append(". ")
                            append(state.etaLabel)
                        }
                        append(". ")
                        append(state.confidenceLabel)
                    }
                },
            ) {
                NearWakeSectionHeader(text = "Route snapshot")
                Text(
                    text = state.routeSummary,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (state.etaLabel.isNotBlank()) {
                    NearWakeNumericText(
                        text = state.etaLabel.filter { it.isDigit() || it == '~' },
                        color = NearWakeColors.MonitoringBase,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Text(
                    text = state.confidenceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SurfaceCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "${state.alertLeadLabel.ifBlank { "Lead time unavailable" }}. ${state.alertIntensityLabel.ifBlank { "Alert intensity unavailable" }}"
                },
            ) {
                NearWakeSectionHeader(text = "Alert setup")
                Text(
                    text = state.alertLeadLabel.ifBlank { "Lead time unavailable" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.alertIntensityLabel.ifBlank { "Alert intensity unavailable" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun summaryAccent(statusLabel: String) = when (statusLabel) {
    "Completed" -> NearWakeColors.SafeBase
    "Monitoring in progress" -> NearWakeColors.MonitoringBase
    else -> NearWakeColors.ApproachBase
}

private fun summaryChipState(statusLabel: String) = when (statusLabel) {
    "Completed" -> NearWakeChipState.Safe
    "Monitoring in progress" -> NearWakeChipState.Monitoring
    else -> NearWakeChipState.Approaching
}
