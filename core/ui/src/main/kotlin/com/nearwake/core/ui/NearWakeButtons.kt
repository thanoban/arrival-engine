package com.nearwake.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nearwake.core.designsystem.LocalRadius
import com.nearwake.core.designsystem.LocalStateAccent
import com.nearwake.core.designsystem.NearWakeColors

@Composable
fun NearWakePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = LocalStateAccent.current,
) {
    val radius = LocalRadius.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = Color.Black,
            disabledContainerColor = NearWakeColors.BgHighest,
            disabledContentColor = NearWakeColors.TextTertiary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun NearWakeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val radius = LocalRadius.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(radius.md),
        border = androidx.compose.foundation.BorderStroke(1.dp, NearWakeColors.BorderDefault),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NearWakeColors.TextPrimary,
            disabledContentColor = NearWakeColors.TextDisabled,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun NearWakeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = NearWakeColors.TextSecondary,
            disabledContentColor = NearWakeColors.TextDisabled,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
