package com.nearwake.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nearwake.core.designsystem.LocalNearWakeColors
import com.nearwake.core.designsystem.LocalSpacing

enum class PlaceResultKind {
    Saved,
    Recent,
    Result,
}

@Composable
fun PlaceResultRow(
    name: String,
    address: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: PlaceResultKind = PlaceResultKind.Result,
    selected: Boolean = false,
    showDivider: Boolean = true,
) {
    val colors = LocalNearWakeColors.current
    val spacing = LocalSpacing.current
    val icon = kind.icon()
    val iconTint = when (kind) {
        PlaceResultKind.Result -> colors.monitoringBase
        PlaceResultKind.Saved -> colors.safeBase
        PlaceResultKind.Recent -> colors.textTertiary
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(if (selected) colors.safeBase.copy(alpha = 0.04f) else Color.Transparent)
                .clickable(role = Role.Button, onClick = onClick),
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(colors.safeBase),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (selected) spacing.md else spacing.cardDefault,
                        end = spacing.cardDefault,
                    )
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint,
                )
                Spacer(modifier = Modifier.width(spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = spacing.massive),
                thickness = 1.dp,
                color = colors.borderSubtle,
            )
        }
    }
}

private fun PlaceResultKind.icon(): ImageVector =
    when (this) {
        PlaceResultKind.Saved -> Icons.Filled.Place
        PlaceResultKind.Recent -> Icons.Filled.History
        PlaceResultKind.Result -> Icons.Filled.LocationOn
    }
