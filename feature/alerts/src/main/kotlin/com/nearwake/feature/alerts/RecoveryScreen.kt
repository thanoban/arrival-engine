package com.nearwake.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip

@Composable
fun RecoveryScreen(
    onEndTrip: () -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    NearWakeScaffold(
        title = "Trip recovery",
        subtitle = "Monitoring stopped late enough that you may have passed the destination.",
    ) {
        HeroCard(accent = NearWakeColors.ApproachBase) {
            NearWakeStateChip(
                label = "Monitoring interrupted",
                state = NearWakeChipState.Approaching,
            )
            Text(
                text = state.destinationName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Last session update: ${state.missedByLabel}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ElevatedCard {
            NearWakeSectionHeader(text = "What to do next")
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = "End the trip once you have re-oriented yourself.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Diagnostics and richer resume controls can build on this screen later, but the current flow stays honest to the available data and actions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        NearWakePrimaryButton(
            text = "End trip",
            accent = NearWakeColors.AlertBase,
            onClick = { viewModel.endTrip(onEndTrip) },
        )
    }
}
