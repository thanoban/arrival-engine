package com.nearwake.core.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalRadius
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.LocalStateAccent
import com.nearwake.core.designsystem.NearWakeMotion

enum class NearWakeChipState {
    Safe,
    Monitoring,
    Approaching,
    Alert,
    Neutral,
}

@Composable
fun NearWakeStateChip(
    label: String,
    state: NearWakeChipState,
    modifier: Modifier = Modifier,
) {
    val radius = LocalRadius.current
    val colors = state.colors()
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 28.dp)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = state.a11yLabel
            },
        shape = RoundedCornerShape(radius.sm),
        color = colors.soft,
        contentColor = colors.base,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = colors.base,
        )
    }
}

@Composable
fun NearWakeSelectableChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radius = LocalRadius.current
    val accent = LocalStateAccent.current
    val colors = LocalNearWakeColors.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        shape = RoundedCornerShape(radius.md),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.borderDefault,
            selectedBorderColor = accent.copy(alpha = 0.65f),
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.bgElevated,
            selectedContainerColor = accent.copy(alpha = 0.14f),
            labelColor = colors.textSecondary,
            selectedLabelColor = accent,
        ),
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
    )
}

private val NearWakeChipState.a11yLabel: String
    get() = when (this) {
        NearWakeChipState.Safe -> "Safe"
        NearWakeChipState.Monitoring -> "Monitoring"
        NearWakeChipState.Approaching -> "Approaching"
        NearWakeChipState.Alert -> "Alert"
        NearWakeChipState.Neutral -> "Neutral"
    }

@Composable
fun NearWakeSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNearWakeColors.current
    Text(
        text = text.uppercase(),
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
        color = colors.textTertiary,
    )
}

private data class ChipColors(
    val base: Color,
    val soft: Color,
    val border: Color,
)

@Composable
private fun NearWakeChipState.colors(): ChipColors {
    val colors = LocalNearWakeColors.current
    return when (this) {
        NearWakeChipState.Safe -> ChipColors(colors.safeBase, colors.safeSoft, colors.safeBorder)
        NearWakeChipState.Monitoring -> ChipColors(
            colors.monitoringBase,
            colors.monitoringSoft,
            colors.monitoringBorder,
        )
        NearWakeChipState.Approaching -> ChipColors(colors.approachBase, colors.approachSoft, colors.approachBorder)
        NearWakeChipState.Alert -> ChipColors(colors.alertBase, colors.alertSoft, colors.alertBorder)
        NearWakeChipState.Neutral -> ChipColors(colors.textSecondary, colors.bgHighest, colors.borderDefault)
    }
}

@Composable
fun NearWakeStatusRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun NearWakeNumericText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(fontFeatureSettings = "tnum"),
    )
}

@Composable
fun PulseRing(
    color: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 280.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse-ring")
    val scale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2000,
                easing = NearWakeMotion.EasingEmphasis,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse-scale",
    )
    val alpha = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2000,
                easing = NearWakeMotion.EasingEmphasis,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse-alpha",
    )

    Canvas(modifier = modifier.size(diameter)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = this.size.minDimension * 0.34f
        repeat(3) { index ->
            drawCircle(
                color = color.copy(alpha = alpha.value * (0.8f - index * 0.18f)),
                radius = baseRadius * (1f + index * 0.16f) * scale.value,
                center = center,
                style = Stroke(width = 4.dp.toPx()),
            )
        }
    }
}
