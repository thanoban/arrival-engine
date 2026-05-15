package com.nearwake.feature.history

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.NearWakeTextButton
import com.nearwake.core.ui.SurfaceCard

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onTripSelected: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.exportCsvText) {
        val csv = state.exportCsvText ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "NearWake trip log")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "Export trip log"))
        viewModel.clearExport()
    }

    ProvideNearWakeStateAccent(NearWakeColors.MonitoringBase) {
        NearWakeScaffold(
            title = "Trip history",
            subtitle = null,
            topBarActions = {
                if (state.trips.isNotEmpty()) {
                    NearWakeTextButton(text = "Export", onClick = { viewModel.exportTrips() })
                }
                NearWakeTextButton(text = "Back", onClick = onBack)
            },
        ) {
            NearWakeSectionHeader(text = "All trips")
            if (state.trips.isEmpty()) {
                SurfaceCard(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "No trips yet. Start a trip from the destination picker and it will appear here."
                    },
                ) {
                    Text(
                        text = "No trips yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Start a trip from the destination picker and it will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                state.trips.forEach { trip ->
                    SurfaceCard(
                        modifier = Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = buildString {
                                    append(trip.destinationName)
                                    append(". ")
                                    append(trip.statusLabel)
                                    if (trip.subtitle.isNotBlank()) {
                                        append(". ")
                                        append(trip.subtitle)
                                    }
                                }
                            }
                            .clickable { onTripSelected(trip.tripId) },
                    ) {
                        NearWakeStateChip(
                            label = trip.statusLabel,
                            state = when (trip.statusLabel) {
                                "Completed" -> NearWakeChipState.Safe
                                "Monitoring now" -> NearWakeChipState.Monitoring
                                else -> NearWakeChipState.Approaching
                            },
                        )
                        Text(
                            text = trip.destinationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = trip.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
