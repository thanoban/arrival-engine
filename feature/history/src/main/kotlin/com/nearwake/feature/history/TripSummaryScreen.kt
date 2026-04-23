package com.nearwake.feature.history

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.SurfaceCard

@Composable
fun TripSummaryScreen(
    onBack: () -> Unit,
    viewModel: TripSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NearWakeScaffold(
        title = state.destinationName,
        subtitle = state.destinationAddress.ifBlank { "Trip summary" },
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        HeroCard(accent = summaryAccent(state.statusLabel)) {
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

        ElevatedCard {
            NearWakeSectionHeader(text = "Route snapshot")
            Text(state.routeSummary, style = MaterialTheme.typography.titleMedium)
            if (state.etaLabel.isNotBlank()) {
                Text(state.etaLabel, color = MaterialTheme.colorScheme.primary)
            }
            Text(state.confidenceLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Alert setup")
            Text(state.alertLeadLabel.ifBlank { "Lead time unavailable" })
            Text(state.alertIntensityLabel.ifBlank { "Alert intensity unavailable" })
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
