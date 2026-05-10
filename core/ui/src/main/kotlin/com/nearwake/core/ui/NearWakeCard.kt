package com.nearwake.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalRadius
import com.nearwake.core.designsystem.LocalSpacing
import com.nearwake.core.designsystem.LocalStateAccent

@Composable
fun NearWakeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    SurfaceCard(modifier = modifier, content = content)
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalNearWakeColors.current
    val spacing = LocalSpacing.current
    NearWakeCardFrame(
        modifier = modifier,
        containerColor = colors.bgSurface,
        borderColor = colors.borderSubtle,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LocalRadius.current.md),
        padding = if (compact) spacing.cardCompact else spacing.cardDefault,
        content = content,
    )
}

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalNearWakeColors.current
    NearWakeCardFrame(
        modifier = modifier,
        containerColor = colors.bgElevated,
        borderColor = colors.borderDefault,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LocalRadius.current.lg),
        padding = LocalSpacing.current.cardLarge,
        content = content,
    )
}

@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    accent: Color = LocalStateAccent.current,
    content: @Composable ColumnScope.() -> Unit,
) {
    NearWakeCardFrame(
        modifier = modifier,
        containerColor = accent.copy(alpha = 0.08f),
        borderColor = accent.copy(alpha = 0.3f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LocalRadius.current.lg),
        padding = LocalSpacing.current.cardLarge,
        content = content,
    )
}

@Composable
private fun NearWakeCardFrame(
    modifier: Modifier,
    containerColor: Color,
    borderColor: Color,
    shape: androidx.compose.ui.graphics.Shape,
    padding: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = shape,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}
