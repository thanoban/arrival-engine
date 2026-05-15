package com.nearwake.feature.alerts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeButtonSize
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeStateChip

@Composable
fun RecoveryScreen(
    onEndTrip: () -> Unit,
    onResumeMonitoring: (String) -> Unit,
    viewModel: RecoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    ProvideNearWakeStateAccent(NearWakeColors.ApproachBase) {
        NearWakeScaffold(
            title = "Trip recovery",
            subtitle = null,
        ) {
            ElevatedCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append("Trip recovery. Monitoring interrupted. ")
                        append("You may have passed your stop at ${state.destinationName}. ")
                        append(state.missedByLabel)
                        append(". ")
                        append(state.returnStopLabel ?: state.recoveryGuidanceLabel)
                    }
                },
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = NearWakeColors.ApproachBase,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        NearWakeStateChip(
                            label = "Monitoring interrupted",
                            state = NearWakeChipState.Approaching,
                        )
                        Text(
                            text = "You may have passed your stop",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "${state.destinationName} · ${state.missedByLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = state.returnStopLabel ?: state.recoveryGuidanceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (state.canResumeMonitoring) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    NearWakeSecondaryButton(
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = "Resume monitoring for ${state.destinationName}"
                            },
                        text = "Re-arm",
                        onClick = { viewModel.resumeMonitoring(onResumeMonitoring) },
                    )
                    NearWakePrimaryButton(
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = "End trip to ${state.destinationName}"
                            },
                        text = "End trip",
                        accent = NearWakeColors.AlertBase,
                        onClick = { viewModel.endTrip(onEndTrip) },
                        size = NearWakeButtonSize.Medium,
                    )
                }
            } else {
                NearWakePrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "End trip to ${state.destinationName}"
                        },
                    text = "End trip",
                    accent = NearWakeColors.AlertBase,
                    onClick = { viewModel.endTrip(onEndTrip) },
                    size = NearWakeButtonSize.Medium,
                )
            }
        }
    }
}
