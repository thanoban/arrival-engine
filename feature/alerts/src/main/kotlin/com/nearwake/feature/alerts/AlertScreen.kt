package com.nearwake.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.ui.NearWakeNumericText
import com.nearwake.core.ui.PulseRing

@Composable
fun AlertScreen(
    onDismiss: (String) -> Unit,
    onRecovery: (String) -> Unit,
    viewModel: AlertViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NearWakeColors.AlertIntense)
            .padding(horizontal = spacing.xl, vertical = spacing.massive),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Spacer at top — no nav chrome on alert screen
            Spacer(modifier = Modifier.size(spacing.xxl))

            // Hero content
            Column(
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = buildString {
                        append(state.destinationName)
                        append(". ")
                        append(
                            state.etaMinutes?.let { minutes ->
                                "Approximately $minutes minutes remaining."
                            } ?: "Arrival alert active. Exit now.",
                        )
                    }
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = state.destinationName,
                    style = MaterialTheme.typography.titleLarge,
                    color = NearWakeColors.TextPrimary,
                )
                Box(contentAlignment = Alignment.Center) {
                    PulseRing(
                        color = NearWakeColors.TextPrimary.copy(alpha = 0.25f),
                        diameter = 192.dp,
                    )
                    val etaMinutes = state.etaMinutes
                    if (etaMinutes != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            NearWakeNumericText(
                                text = etaMinutes.toString(),
                                color = NearWakeColors.TextPrimary,
                                style = MaterialTheme.typography.displayMedium,
                            )
                            Text(
                                text = "min",
                                style = MaterialTheme.typography.titleSmall,
                                color = NearWakeColors.TextPrimary.copy(alpha = 0.85f),
                            )
                        }
                    } else {
                        Text(
                            text = "EXIT NOW",
                            style = MaterialTheme.typography.headlineLarge,
                            color = NearWakeColors.TextPrimary,
                        )
                    }
                }
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Button(
                    onClick = { viewModel.enterWalkFinish(onDismiss) },
                    modifier = Modifier
                        .size(128.dp)
                        .semantics {
                            contentDescription = "Walk to the final destination"
                        },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NearWakeColors.TextPrimary,
                        contentColor = NearWakeColors.AlertIntense,
                    ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                        )
                        Text(
                            text = "Walk",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                TextButton(
                    onClick = { onRecovery(state.tripId) },
                    modifier = Modifier.semantics {
                        contentDescription = "I missed my stop. Open trip recovery."
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NearWakeColors.TextPrimary.copy(alpha = 0.7f),
                    ),
                ) {
                    Text(
                        text = "I missed it",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
