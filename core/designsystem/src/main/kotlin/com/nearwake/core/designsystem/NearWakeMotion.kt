package com.nearwake.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing

object NearWakeMotion {
    const val Fast = 150
    const val Base = 220
    const val Slow = 400

    val EasingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasis = CubicBezierEasing(0.3f, 0f, 0f, 1f)
}
