package com.nearwake.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalStateAccent = staticCompositionLocalOf { NearWakeColors.SafeBase }
val LocalNearWakeColors = staticCompositionLocalOf { DarkNearWakeColors }

private val DarkNearWakeColorScheme = darkColorScheme(
    primary = DarkNearWakeColors.safeBase,
    onPrimary = Color.Black,
    secondary = DarkNearWakeColors.monitoringBase,
    tertiary = DarkNearWakeColors.approachBase,
    background = DarkNearWakeColors.bgBase,
    surface = DarkNearWakeColors.bgSurface,
    surfaceVariant = DarkNearWakeColors.bgElevated,
    error = DarkNearWakeColors.alertBase,
    outline = DarkNearWakeColors.borderDefault,
    outlineVariant = DarkNearWakeColors.borderSubtle,
    onBackground = DarkNearWakeColors.textPrimary,
    onSurface = DarkNearWakeColors.textPrimary,
    onSurfaceVariant = DarkNearWakeColors.textSecondary,
    onError = DarkNearWakeColors.textPrimary,
)

private val LightNearWakeColorScheme = lightColorScheme(
    primary = LightNearWakeColors.safeBase,
    onPrimary = Color.White,
    secondary = LightNearWakeColors.monitoringBase,
    tertiary = LightNearWakeColors.approachBase,
    background = LightNearWakeColors.bgBase,
    surface = LightNearWakeColors.bgSurface,
    surfaceVariant = LightNearWakeColors.bgElevated,
    error = LightNearWakeColors.alertBase,
    outline = LightNearWakeColors.borderDefault,
    outlineVariant = LightNearWakeColors.borderSubtle,
    onBackground = LightNearWakeColors.textPrimary,
    onSurface = LightNearWakeColors.textPrimary,
    onSurfaceVariant = LightNearWakeColors.textSecondary,
    onError = Color.White,
)

@Composable
fun NearWakeTheme(
    darkTheme: Boolean = true,
    stateAccent: Color = NearWakeColors.SafeBase,
    content: @Composable () -> Unit,
) {
    val nearWakeColors = if (darkTheme) DarkNearWakeColors else LightNearWakeColors
    val baseColorScheme = if (darkTheme) DarkNearWakeColorScheme else LightNearWakeColorScheme
    val colorScheme = baseColorScheme.copy(primary = stateAccent)
    CompositionLocalProvider(
        LocalSpacing provides NearWakeSpacing(),
        LocalRadius provides NearWakeRadius(),
        LocalNearWakeColors provides nearWakeColors,
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
