package com.nearwake.feature.departure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun DepartureReminderScreen(
    onBack: () -> Unit,
    onStartTrip: (destinationId: String) -> Unit,
    viewModel: DepartureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    ProvideNearWakeStateAccent(NearWakeColors.SafeBase) {
        NearWakeScaffold(
            title = "Leave by",
            subtitle = "Based on your trip history",
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            if (state.isRefreshing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                }
            }

            if (state.predictions.isEmpty() && !state.isRefreshing) {
                SurfaceCard {
                    NearWakeSectionHeader(text = "No patterns yet")
                    Text(
                        text = "Complete a few trips to the same destination and NearWake will learn your typical departure times.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                NearWakeSectionHeader(text = "Today's departures")
                state.predictions.forEach { prediction ->
                    SurfaceCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NearWakeStateChip(
                                label = prediction.leaveByLabel,
                                state = NearWakeChipState.Safe,
                            )
                            NearWakeStateChip(
                                label = prediction.routeLabel,
                                state = NearWakeChipState.Monitoring,
                            )
                        }
                        Text(
                            text = prediction.destinationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Based on ${prediction.tripCount} previous trips",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        NearWakePrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Start trip to ${prediction.destinationName}",
                            onClick = { onStartTrip(prediction.destinationId) },
                        )
                    }
                }
            }
        }
    }
}
