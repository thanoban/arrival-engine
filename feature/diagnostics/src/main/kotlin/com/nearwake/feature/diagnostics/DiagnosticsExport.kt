package com.nearwake.feature.diagnostics

internal fun buildDiagnosticsExport(state: DiagnosticsUiState): String = buildString {
    appendLine("NearWake diagnostics export")
    appendLine()
    appendLine("Service state: ${state.stateLabel}")
    appendLine()
    appendLine("Registered geofences:")
    if (state.registeredGeofences.isEmpty()) {
        appendLine("- none")
    } else {
        state.registeredGeofences.forEach { geofence ->
            appendLine("- $geofence")
        }
    }
    appendLine()
    appendLine("Recent diagnostics events:")
    if (state.recentEvents.isEmpty()) {
        appendLine("- none recorded")
    } else {
        state.recentEvents.forEach { event ->
            val title = event.tripPrefix?.let { "${event.recordedAtLabel} ${event.label} [$it]" }
                ?: "${event.recordedAtLabel} ${event.label}"
            appendLine("- $title")
            appendLine("  ${event.summary}")
        }
    }
}
