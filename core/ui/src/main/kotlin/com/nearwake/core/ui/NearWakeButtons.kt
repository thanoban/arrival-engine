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
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalRadius
import com.nearwake.core.designsystem.LocalStateAccent

enum class NearWakeButtonSize {
    Small,
    Medium,
    Large,
}

@Composable
fun NearWakePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = LocalStateAccent.current,
    size: NearWakeButtonSize = NearWakeButtonSize.Medium,
) {
    val radius = LocalRadius.current
    val colors = LocalNearWakeColors.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = size.minHeight),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = accent,
            contentColor = Color.Black,
            disabledContainerColor = colors.bgHighest,
            disabledContentColor = colors.textTertiary,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

private val NearWakeButtonSize.minHeight
    get() = when (this) {
        NearWakeButtonSize.Small -> 40.dp
        NearWakeButtonSize.Medium -> 48.dp
        NearWakeButtonSize.Large -> 56.dp
    }

@Composable
fun NearWakeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val radius = LocalRadius.current
    val colors = LocalNearWakeColors.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(radius.md),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderDefault),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.textPrimary,
            disabledContentColor = colors.textDisabled,
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
    val colors = LocalNearWakeColors.current
    androidx.compose.material3.TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = colors.textSecondary,
            disabledContentColor = colors.textDisabled,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}
