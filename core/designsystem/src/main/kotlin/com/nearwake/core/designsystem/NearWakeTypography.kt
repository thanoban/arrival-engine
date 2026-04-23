package com.nearwake.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry
import androidx.compose.ui.text.googlefonts.GoogleFont.Provider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val InterProvider = Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val InterFamily = FontFamily(
    GoogleFontEntry(
        googleFont = GoogleFont("Inter"),
        fontProvider = InterProvider,
        weight = FontWeight.Normal,
    ),
    GoogleFontEntry(
        googleFont = GoogleFont("Inter"),
        fontProvider = InterProvider,
        weight = FontWeight.Medium,
    ),
    GoogleFontEntry(
        googleFont = GoogleFont("Inter"),
        fontProvider = InterProvider,
        weight = FontWeight.SemiBold,
    ),
)

object NearWakeType {
    val DisplayXL = TextStyle(
        fontFamily = InterFamily,
        fontSize = 64.sp,
        lineHeight = 68.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1).sp,
    )
    val DisplayL = TextStyle(
        fontFamily = InterFamily,
        fontSize = 48.sp,
        lineHeight = 54.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp,
    )
    val Headline = TextStyle(
        fontFamily = InterFamily,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
    )
    val Title = TextStyle(
        fontFamily = InterFamily,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    )
    val BodyL = TextStyle(
        fontFamily = InterFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    )
    val Body = TextStyle(
        fontFamily = InterFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    )
    val Label = TextStyle(
        fontFamily = InterFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    )
    val Numeric = DisplayXL.copy(fontFeatureSettings = "tnum", textAlign = TextAlign.Center)
}

val NearWakeTypography = Typography(
    displayLarge = NearWakeType.DisplayL,
    displayMedium = NearWakeType.DisplayXL,
    headlineLarge = NearWakeType.Headline,
    headlineMedium = NearWakeType.Headline,
    titleLarge = NearWakeType.Title,
    titleMedium = NearWakeType.Title.copy(fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = NearWakeType.BodyL,
    bodyMedium = NearWakeType.Body,
    labelLarge = NearWakeType.Label.copy(letterSpacing = 0.2.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = NearWakeType.Label,
    labelSmall = NearWakeType.Label.copy(letterSpacing = 0.5.sp),
)
