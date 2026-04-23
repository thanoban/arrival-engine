package com.nearwake.feature.tripsetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSecondaryButton
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeSelectableChip
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard
import com.nearwake.domain.trip.model.AlertIntensity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripSetupScreen(
    onStartTrip: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TripSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val routeReady = state.etaLabel.startsWith("~")
    NearWakeScaffold(
        title = "Set up trip",
        subtitle = "Tune the lead time, preview the route, then arm the trip when you are ready.",
        topBarActions = {
            NearWakeSecondaryButton(text = "Back", onClick = onBack)
        },
    ) {
        ElevatedCard {
            NearWakeSectionHeader(text = "Destination")
            Text(state.destinationName, style = MaterialTheme.typography.titleLarge)
            if (state.destinationAddress.isNotBlank()) {
                Text(
                    state.destinationAddress,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.padding(top = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                NearWakeStateChip(
                    label = if (routeReady) "Route ready" else "Destination-only",
                    state = if (routeReady) NearWakeChipState.Safe else NearWakeChipState.Neutral,
                )
                NearWakeStateChip(
                    label = state.etaLabel,
                    state = if (routeReady) NearWakeChipState.Monitoring else NearWakeChipState.Approaching,
                )
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Route preview")
            Text(
                text = state.routeSummary,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "NearWake will keep monitoring even if route data disappears after the trip starts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            NearWakeSectionHeader(text = "Alert me")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                listOf(2, 5, 10, 15).forEach { minutes ->
                    NearWakeSelectableChip(
                        selected = state.alertLeadMinutes == minutes,
                        label = "${minutes} min",
                        onClick = { viewModel.selectLeadMinutes(minutes) },
                    )
                }
                NearWakeSelectableChip(
                    selected = state.alertLeadMinutes == 0,
                    label = "Nearby",
                    onClick = { viewModel.selectLeadMinutes(0) },
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            NearWakeSectionHeader(text = "Alert style")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                AlertIntensity.entries.forEach { intensity ->
                    NearWakeSelectableChip(
                        selected = state.alertIntensity == intensity,
                        label = intensity.name.lowercase().replaceFirstChar(Char::uppercase),
                        onClick = { viewModel.selectIntensity(intensity) },
                    )
                }
            }
        }

        SurfaceCard {
            NearWakeSectionHeader(text = "Monitoring")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Text("Background monitoring", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Keeps the service alive while you lock the screen or switch apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.backgroundMonitoringEnabled,
                    onCheckedChange = viewModel::setBackgroundMonitoringEnabled,
                )
            }
        }

        NearWakePrimaryButton(
            modifier = Modifier.padding(top = spacing.md),
            text = "Arm trip",
            onClick = { viewModel.startTrip(onStartTrip) },
            enabled = state.canStart,
        )
    }
}
