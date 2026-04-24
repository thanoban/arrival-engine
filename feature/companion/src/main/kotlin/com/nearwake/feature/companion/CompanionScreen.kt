package com.nearwake.feature.companion

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearwake.core.designsystem.NearWakeColors
import com.nearwake.core.ui.ElevatedCard
import com.nearwake.core.ui.HeroCard
import com.nearwake.core.ui.NearWakeChipState
import com.nearwake.core.ui.NearWakePrimaryButton
import com.nearwake.core.ui.NearWakeScaffold
import com.nearwake.core.ui.NearWakeSectionHeader
import com.nearwake.core.ui.NearWakeStateChip

@Composable
fun CompanionScreen(
    onDone: () -> Unit,
    viewModel: CompanionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NearWakeScaffold(
        title = "Arrival confirmation",
        subtitle = "Send a one-shot update that you made it.",
    ) {
        HeroCard(accent = NearWakeColors.SafeBase) {
            NearWakeStateChip(
                label = state.title,
                state = NearWakeChipState.Safe,
            )
            Text(
                text = state.messagePreview,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        ElevatedCard {
            NearWakeSectionHeader(text = "Preview")
            Text(
                text = state.smsPreview,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        NearWakePrimaryButton(
            text = "Share",
            modifier = Modifier.fillMaxWidth(),
            accent = NearWakeColors.MonitoringBase,
            onClick = {
                context.startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, state.messagePreview)
                        },
                        "Share arrival confirmation",
                    ),
                )
            },
        )

        NearWakePrimaryButton(
            text = "Send SMS",
            modifier = Modifier.fillMaxWidth(),
            accent = NearWakeColors.ApproachBase,
            onClick = {
                ContextCompat.startActivity(
                    context,
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:")
                        putExtra("sms_body", state.smsPreview)
                    },
                    null,
                )
            },
        )

        NearWakePrimaryButton(
            text = "Done",
            modifier = Modifier.fillMaxWidth(),
            accent = NearWakeColors.SafeBase,
            onClick = onDone,
        )
    }
}
