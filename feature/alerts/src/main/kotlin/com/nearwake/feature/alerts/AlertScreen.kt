package com.nearwake.feature.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
                            if (state.etaLabel.any { it.isDigit() }) {
                                "${state.etaLabel} remaining."
                            } else {
                                "Arriving now."
                            },
                        )
                    }
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    PulseRing(
                        color = NearWakeColors.TextPrimary.copy(alpha = 0.25f),
                        diameter = spacing.massive * 4,
                    )
                    Spacer(modifier = Modifier.size(spacing.massive * 3))
                }
                Text(
                    text = "ARRIVING",
                    style = MaterialTheme.typography.displayLarge,
                    color = NearWakeColors.TextPrimary,
                )
                Text(
                    text = state.destinationName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = NearWakeColors.TextPrimary,
                )
                NearWakeNumericText(
                    text = state.etaLabel.filter { it.isDigit() }.ifBlank { "0" },
                    color = NearWakeColors.TextPrimary,
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = if (state.etaLabel.any { it.isDigit() }) "minutes remaining" else "arriving now",
                    style = MaterialTheme.typography.titleLarge,
                    color = NearWakeColors.TextPrimary.copy(alpha = 0.85f),
                )
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Button(
                    onClick = { viewModel.enterWalkFinish(onDismiss) },
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NearWakeColors.TextPrimary,
                        contentColor = NearWakeColors.AlertIntense,
                    ),
                ) {
                    Text(
                        text = "Walk",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TextButton(
                    onClick = { onRecovery(state.tripId) },
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
