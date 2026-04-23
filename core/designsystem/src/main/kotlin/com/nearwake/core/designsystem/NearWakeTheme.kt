package com.nearwake.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalStateAccent = staticCompositionLocalOf { NearWakeColors.SafeBase }

private val NearWakeColorScheme = darkColorScheme(
    primary = NearWakeColors.SafeBase,
    onPrimary = Color.Black,
    secondary = NearWakeColors.MonitoringBase,
    tertiary = NearWakeColors.ApproachBase,
    background = NearWakeColors.BgBase,
    surface = NearWakeColors.BgSurface,
    surfaceVariant = NearWakeColors.BgElevated,
    error = NearWakeColors.AlertBase,
    outline = NearWakeColors.BorderDefault,
    outlineVariant = NearWakeColors.BorderSubtle,
    onBackground = NearWakeColors.TextPrimary,
    onSurface = NearWakeColors.TextPrimary,
    onSurfaceVariant = NearWakeColors.TextSecondary,
    onError = NearWakeColors.TextPrimary,
)

@Composable
fun NearWakeTheme(
    darkTheme: Boolean = true,
    stateAccent: Color = NearWakeColors.SafeBase,
    content: @Composable () -> Unit,
) {
    val colorScheme = NearWakeColorScheme.copy(primary = stateAccent)
    CompositionLocalProvider(
        LocalSpacing provides NearWakeSpacing(),
        LocalRadius provides NearWakeRadius(),
        LocalStateAccent provides stateAccent,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NearWakeTypography,
            content = content,
        )
    }
}

@Composable
fun ProvideNearWakeStateAccent(
    accent: Color,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalStateAccent provides accent, content = content)
}
