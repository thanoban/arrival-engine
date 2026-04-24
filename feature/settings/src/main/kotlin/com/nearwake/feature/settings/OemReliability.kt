package com.nearwake.feature.settings

import java.util.Locale

internal data class OemReliabilityGuidance(
    val manufacturerLabel: String,
    val statusLabel: String,
    val statusTone: OemReliabilityTone,
    val summary: String,
    val steps: List<String>,
)

internal enum class OemReliabilityTone {
    Stable,
    Review,
    Degraded,
}

internal fun oemReliabilityGuidance(
    manufacturer: String,
    backgroundMonitoringEnabled: Boolean,
): OemReliabilityGuidance {
    val normalized = manufacturer.trim().lowercase(Locale.ROOT)
    val manufacturerLabel = manufacturer.trim().ifBlank { "Android device" }
    if (!backgroundMonitoringEnabled) {
        return OemReliabilityGuidance(
            manufacturerLabel = manufacturerLabel,
            statusLabel = "Degraded",
            statusTone = OemReliabilityTone.Degraded,
            summary = "Background monitoring is off, so NearWake may miss alerts while your screen is locked.",
            steps = listOf(
                "Turn on background monitoring in NearWake.",
                "Allow notifications so Stage B and Stage C alerts can break through clearly.",
            ),
        )
    }

    return when {
        normalized.contains("samsung") -> OemReliabilityGuidance(
            manufacturerLabel = "Samsung",
            statusLabel = "Review battery settings",
            statusTone = OemReliabilityTone.Review,
            summary = "Samsung often pauses background services aggressively. NearWake cannot verify exemptions yet, so check these steps if alerts feel delayed.",
            steps = listOf(
                "Open Settings > Battery > Background usage limits.",
                "Remove NearWake from Sleeping apps and Deep sleeping apps.",
                "Open App info > Battery and allow unrestricted usage if you want maximum reliability.",
            ),
        )

        normalized.contains("xiaomi") || normalized.contains("redmi") || normalized.contains("mi") -> OemReliabilityGuidance(
            manufacturerLabel = "Xiaomi",
            statusLabel = "Review battery settings",
            statusTone = OemReliabilityTone.Review,
            summary = "MIUI commonly kills background location work. Double-check battery and autostart behavior before relying on sleep-mode alerts.",
            steps = listOf(
                "Open Security or Settings > Battery > App battery saver.",
                "Set NearWake to No restrictions.",
                "Enable Autostart for NearWake if your device exposes it.",
            ),
        )

        normalized.contains("oppo") || normalized.contains("realme") || normalized.contains("vivo") || normalized.contains("oneplus") -> OemReliabilityGuidance(
            manufacturerLabel = manufacturerLabel.replaceFirstChar(Char::uppercase),
            statusLabel = "Review battery settings",
            statusTone = OemReliabilityTone.Review,
            summary = "This OEM family is known for background task restrictions. NearWake should still work, but reliability is better after battery exemptions are reviewed.",
            steps = listOf(
                "Open App info > Battery usage or Battery optimization.",
                "Allow NearWake to run in the background without optimization.",
                "If available, pin NearWake in recents or enable Auto launch.",
            ),
        )

        normalized.contains("google") || normalized.contains("pixel") || normalized.contains("motorola") || normalized.contains("nokia") -> OemReliabilityGuidance(
            manufacturerLabel = manufacturerLabel.replaceFirstChar(Char::uppercase),
            statusLabel = "Standard Android",
            statusTone = OemReliabilityTone.Stable,
            summary = "This device family is usually predictable for background monitoring. Keep notifications and location permissions enabled for the best results.",
            steps = listOf(
                "Leave notifications enabled so arrival alerts remain visible.",
                "Keep location permission available during active trips.",
            ),
        )

        else -> OemReliabilityGuidance(
            manufacturerLabel = manufacturerLabel.replaceFirstChar(Char::uppercase),
            statusLabel = "Check device settings",
            statusTone = OemReliabilityTone.Review,
            summary = "NearWake cannot confirm how this OEM handles battery optimization yet. If alerts ever feel late, review the device's background app restrictions.",
            steps = listOf(
                "Open the system battery settings for NearWake.",
                "Look for optimization, sleep, or background restriction controls.",
                "Allow NearWake to run without aggressive battery limits if you want maximum reliability.",
            ),
        )
    }
}
