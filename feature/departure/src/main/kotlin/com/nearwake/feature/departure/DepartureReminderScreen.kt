package com.nearwake.feature.departure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing
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
    val themeColors = LocalNearWakeColors.current

    ProvideNearWakeStateAccent(themeColors.safeBase) {
        NearWakeScaffold(
            title = "Leave by",
            subtitle = "Based on your trip history",
            topBarActions = {
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            SurfaceCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "Reminder scheduling. ${state.scheduleStatus}. Departure reminders are ${if (state.remindersEnabled) "on" else "off"}."
                },
            ) {
                NearWakeSectionHeader(text = "Reminder scheduling")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.scheduleStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        modifier = Modifier.semantics {
                            contentDescription = "Departure reminders"
                            stateDescription = if (state.remindersEnabled) "On" else "Off"
                        },
                        checked = state.remindersEnabled,
                        onCheckedChange = viewModel::setDepartureRemindersEnabled,
                    )
                }
                Text(
                    text = "NearWake uses flexible Android alarms for leave-by reminders, so delivery may vary by a few minutes to protect battery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            state.errorMessage?.let { errorMessage ->
                SurfaceCard(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Scheduling issue. $errorMessage"
                    },
                ) {
                    NearWakeSectionHeader(text = "Scheduling issue")
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

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
                SurfaceCard(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "No patterns yet. Complete a few trips to the same destination and NearWake will learn your typical departure times."
                    },
                ) {
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
                    SurfaceCard(
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "${prediction.destinationName}. Leave by ${prediction.leaveByLabel}. ${prediction.routeLabel}. Based on ${prediction.tripCount} previous trips."
                        },
                    ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Start trip to ${prediction.destinationName} from your predicted departure plan"
                                },
                            text = "Start trip to ${prediction.destinationName}",
                            onClick = { onStartTrip(prediction.destinationId) },
                        )
                    }
                }
            }
        }
    }
}
