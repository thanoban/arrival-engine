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
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun RecoveryScreen(
    onEndTrip: () -> Unit,
    onResumeMonitoring: (String) -> Unit,
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
            Text(
                text = state.confidenceLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ElevatedCard {
            NearWakeSectionHeader(text = "Last known trip state")
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = state.routeSummary,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = state.lastEtaLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Best recovery move")
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = state.recoveryGuidanceLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                state.returnStopLabel?.let { returnStopLabel ->
                    Text(
                        text = returnStopLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        state.walkBackLabel?.let { walkBackLabel ->
            SurfaceCard {
                NearWakeSectionHeader(text = "Walk-back note")
                Text(
                    text = walkBackLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        if (state.canResumeMonitoring) {
            NearWakeSecondaryButton(
                text = "Resume monitoring",
                onClick = { viewModel.resumeMonitoring(onResumeMonitoring) },
            )
        }

        NearWakePrimaryButton(
            text = "End trip",
            accent = NearWakeColors.AlertBase,
            onClick = { viewModel.endTrip(onEndTrip) },
        )
    }
}
