package com.nearwake.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.designsystem.ProvideNearWakeStateAccent
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeStateChip
import com.nearwake.core.ui.SurfaceCard

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    ProvideNearWakeStateAccent(NearWakeColors.BrandBase) {
        NearWakeScaffold(
            title = "Wake before your stop",
            subtitle = null,
            showTopBarDivider = false,
        ) {
            HeroCard(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "Privacy-first. Background monitoring, quiet until needed, and built for the commutes where fatigue makes timing hard."
                },
            ) {
                NearWakeStateChip(
                    label = "Privacy-first",
                    state = NearWakeChipState.Safe,
                )
                Text(
                    text = "Background monitoring, quiet until needed, and built for the commutes where fatigue makes timing hard.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                state.pages.forEachIndexed { index, page ->
                    SurfaceCard(
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Step ${index + 1}. $page"
                        },
                    ) {
                        Text(
                            text = "0${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = NearWakeColors.MonitoringBase,
                        )
                        Text(
                            text = page,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }

            NearWakePrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Start NearWake setup"
                    },
                text = "Start setup",
                onClick = { viewModel.completeOnboarding(onContinue) },
            )
        }
    }
}
