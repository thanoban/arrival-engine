package com.nearwake.feature.walkfinish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.nearwake.core.ui.SurfaceCard

@Composable
fun WalkFinishScreen(
    onArrived: () -> Unit,
    onShareArrival: (String) -> Unit,
    viewModel: WalkFinishViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    NearWakeScaffold(
        title = "Last mile",
        subtitle = "Lightweight final guidance from your stop to the destination.",
    ) {
        HeroCard(accent = NearWakeColors.SafeBase) {
            NearWakeStateChip(
                label = "Walk finish",
                state = NearWakeChipState.Safe,
            )
            Text(
                text = state.destinationName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (state.destinationAddress.isNotBlank()) {
                Text(
                    text = state.destinationAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = state.instructionLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        ElevatedCard {
            NearWakeSectionHeader(text = "Guidance")
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.distanceLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Direction",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.headingLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Arrival confirmation")
            Text(
                text = state.arrivalHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        NearWakePrimaryButton(
            text = "Confirm arrival",
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            accent = NearWakeColors.SafeBase,
            onClick = { viewModel.confirmArrival(onArrived) },
        )

        NearWakePrimaryButton(
            text = "Confirm and share",
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            accent = NearWakeColors.MonitoringBase,
            onClick = {
                viewModel.confirmArrivalAndShare(onShareArrival)
            },
        )
    }
}
