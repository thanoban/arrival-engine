package com.nearwake.core.designsystem

import androidx.compose.ui.graphics.Color

object NearWakeColors {
    val BgBase = Color(0xFF0A0B0D)
    val BgSurface = Color(0xFF111316)
    val BgElevated = Color(0xFF181B1F)
    val BgHighest = Color(0xFF1F2327)
    val BorderSubtle = Color(0xFF22262B)
    val BorderDefault = Color(0xFF2C3137)

    val TextPrimary = Color(0xFFF5F6F7)
    val TextSecondary = Color(0xFFA8ADB4)
    val TextTertiary = Color(0xFF7C838C)
    val TextDisabled = Color(0xFF434950)

    val SafeBase = Color(0xFF22C55E)
    val SafeSoft = Color(0xFF0F2A1A)
    val SafeBorder = Color(0xFF1A4A2E)

    val MonitoringBase = Color(0xFF38BDF8)
    val MonitoringSoft = Color(0xFF0A2A3A)
    val MonitoringBorder = Color(0xFF164C66)

    val ApproachBase = Color(0xFFF59E0B)
    val ApproachSoft = Color(0xFF2A1F0A)
    val ApproachBorder = Color(0xFF4C3A14)

    val AlertBase = Color(0xFFEF4444)
    val AlertIntense = Color(0xFFFF4444)
    val AlertSoft = Color(0xFF2A0F0F)
    val AlertBorder = Color(0xFF661818)
}

data class NearWakeColorRoles(
    val bgBase: Color,
    val bgSurface: Color,
    val bgElevated: Color,
    val bgHighest: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val safeBase: Color,
    val safeSoft: Color,
    val safeBorder: Color,
    val monitoringBase: Color,
    val monitoringSoft: Color,
    val monitoringBorder: Color,
    val approachBase: Color,
    val approachSoft: Color,
    val approachBorder: Color,
    val alertBase: Color,
    val alertIntense: Color,
    val alertSoft: Color,
    val alertBorder: Color,
)

val DarkNearWakeColors = NearWakeColorRoles(
    bgBase = NearWakeColors.BgBase,
    bgSurface = NearWakeColors.BgSurface,
    bgElevated = NearWakeColors.BgElevated,
    bgHighest = NearWakeColors.BgHighest,
    borderSubtle = NearWakeColors.BorderSubtle,
    borderDefault = NearWakeColors.BorderDefault,
    textPrimary = NearWakeColors.TextPrimary,
    textSecondary = NearWakeColors.TextSecondary,
    textTertiary = NearWakeColors.TextTertiary,
    textDisabled = NearWakeColors.TextDisabled,
    safeBase = NearWakeColors.SafeBase,
    safeSoft = NearWakeColors.SafeSoft,
    safeBorder = NearWakeColors.SafeBorder,
    monitoringBase = NearWakeColors.MonitoringBase,
    monitoringSoft = NearWakeColors.MonitoringSoft,
    monitoringBorder = NearWakeColors.MonitoringBorder,
    approachBase = NearWakeColors.ApproachBase,
    approachSoft = NearWakeColors.ApproachSoft,
    approachBorder = NearWakeColors.ApproachBorder,
    alertBase = NearWakeColors.AlertBase,
    alertIntense = NearWakeColors.AlertIntense,
    alertSoft = NearWakeColors.AlertSoft,
    alertBorder = NearWakeColors.AlertBorder,
)

val LightNearWakeColors = NearWakeColorRoles(
    bgBase = Color(0xFFF7FAF8),
    bgSurface = Color(0xFFFFFFFF),
    bgElevated = Color(0xFFF0F5F2),
    bgHighest = Color(0xFFE5ECE8),
    borderSubtle = Color(0xFFD9E2DD),
    borderDefault = Color(0xFFB8C6BF),
    textPrimary = Color(0xFF17211B),
    textSecondary = Color(0xFF516158),
    textTertiary = Color(0xFF606D67),
    textDisabled = Color(0xFFA5AFA9),
    safeBase = Color(0xFF166534),
    safeSoft = Color(0xFFE5F6EA),
    safeBorder = Color(0xFFB8E7C4),
    monitoringBase = Color(0xFF0369A1),
    monitoringSoft = Color(0xFFE4F4FC),
    monitoringBorder = Color(0xFFB8E0F5),
    approachBase = Color(0xFFB45309),
    approachSoft = Color(0xFFFFF4DA),
    approachBorder = Color(0xFFFBD38D),
    alertBase = Color(0xFFC62828),
    alertIntense = Color(0xFFB91C1C),
    alertSoft = Color(0xFFFFE8E8),
    alertBorder = Color(0xFFF8B4B4),
)
