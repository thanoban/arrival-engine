package com.nearwake.feature.diagnostics

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
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    ProvideNearWakeStateAccent(NearWakeColors.MonitoringBase) {
        NearWakeScaffold(
            title = "Diagnostics",
            subtitle = null,
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            SurfaceCard {
                NearWakeSectionHeader(text = "Services")
                NearWakeStateChip(
                    label = state.stateLabel.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase),
                    state = if (state.stateLabel == "No active trip") NearWakeChipState.Neutral else NearWakeChipState.Monitoring,
                )
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Location")
                Text(
                    text = "Registered geofences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    if (state.registeredGeofences.isEmpty()) {
                        Text(
                            text = "No geofences registered",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.registeredGeofences.forEach { geofence ->
                            Text(
                                text = geofence,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            SurfaceCard {
                NearWakeSectionHeader(text = "Last session")
                Text(
                    text = "Recent events",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    if (state.recentEvents.isEmpty()) {
                        Text(
                            text = "No diagnostics events recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.recentEvents.forEach { event ->
                            Text(
                                text = event,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }
    }
}
