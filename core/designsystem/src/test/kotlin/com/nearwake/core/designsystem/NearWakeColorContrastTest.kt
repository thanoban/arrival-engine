package com.nearwake.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NearWakeColorContrastTest {
    @Test
    fun `dark text roles maintain readable contrast`() {
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.textPrimary,
            background = DarkNearWakeColors.bgBase,
            minimumRatio = 7.0,
            label = "dark textPrimary on bgBase",
        )
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.textSecondary,
            background = DarkNearWakeColors.bgSurface,
            minimumRatio = 4.5,
            label = "dark textSecondary on bgSurface",
        )
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.textTertiary,
            background = DarkNearWakeColors.bgElevated,
            minimumRatio = 4.5,
            label = "dark textTertiary on bgElevated",
        )
    }

    @Test
    fun `light text roles maintain readable contrast`() {
        assertContrastAtLeast(
            foreground = LightNearWakeColors.textPrimary,
            background = LightNearWakeColors.bgBase,
            minimumRatio = 7.0,
            label = "light textPrimary on bgBase",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.textSecondary,
            background = LightNearWakeColors.bgSurface,
            minimumRatio = 4.5,
            label = "light textSecondary on bgSurface",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.textTertiary,
            background = LightNearWakeColors.bgElevated,
            minimumRatio = 4.5,
            label = "light textTertiary on bgElevated",
        )
    }

    @Test
    fun `state accents remain readable on tinted cards`() {
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.safeBase,
            background = DarkNearWakeColors.safeSoft,
            minimumRatio = 4.5,
            label = "dark safeBase on safeSoft",
        )
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.monitoringBase,
            background = DarkNearWakeColors.monitoringSoft,
            minimumRatio = 4.5,
            label = "dark monitoringBase on monitoringSoft",
        )
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.approachBase,
            background = DarkNearWakeColors.approachSoft,
            minimumRatio = 4.5,
            label = "dark approachBase on approachSoft",
        )
        assertContrastAtLeast(
            foreground = DarkNearWakeColors.alertBase,
            background = DarkNearWakeColors.alertSoft,
            minimumRatio = 4.5,
            label = "dark alertBase on alertSoft",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.safeBase,
            background = LightNearWakeColors.safeSoft,
            minimumRatio = 4.5,
            label = "light safeBase on safeSoft",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.monitoringBase,
            background = LightNearWakeColors.monitoringSoft,
            minimumRatio = 4.5,
            label = "light monitoringBase on monitoringSoft",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.approachBase,
            background = LightNearWakeColors.approachSoft,
            minimumRatio = 4.5,
            label = "light approachBase on approachSoft",
        )
        assertContrastAtLeast(
            foreground = LightNearWakeColors.alertBase,
            background = LightNearWakeColors.alertSoft,
            minimumRatio = 4.5,
            label = "light alertBase on alertSoft",
        )
    }

    private fun assertContrastAtLeast(
        foreground: Color,
        background: Color,
        minimumRatio: Double,
        label: String,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            ratio >= minimumRatio,
            "$label contrast was %.2f but needed at least %.2f".format(ratio, minimumRatio),
        )
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double {
        val red = linearize(color.red.toDouble())
        val green = linearize(color.green.toDouble())
        val blue = linearize(color.blue.toDouble())
        return (0.2126 * red) + (0.7152 * green) + (0.0722 * blue)
    }

    private fun linearize(channel: Double): Double =
        if (channel <= 0.03928) {
            channel / 12.92
        } else {
            Math.pow((channel + 0.055) / 1.055, 2.4)
        }
}
