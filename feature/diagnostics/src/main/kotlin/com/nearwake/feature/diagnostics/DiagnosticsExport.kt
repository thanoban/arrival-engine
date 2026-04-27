package com.nearwake.feature.diagnostics

internal fun buildDiagnosticsExport(state: DiagnosticsUiState): String = buildString {
    appendLine("NearWake diagnostics export")
    appendLine()
    appendLine("App version: ${state.buildInfo.appVersionLabel}")
    appendLine("Build type: ${state.buildInfo.buildTypeLabel}")
    appendLine("Git revision: ${state.buildInfo.gitShaLabel}")
    appendLine("Device: ${state.buildInfo.deviceLabel}")
    appendLine()
    appendLine("Permission readiness: ${state.permissions.readinessLabel}")
    appendLine(state.permissions.summary)
    state.permissions.statuses.filter { it.relevant }.forEach { permission ->
        appendLine("${permission.title}: ${if (permission.granted) "Granted" else "Needed"}")
    }
    appendLine()
    appendLine("Power saver: ${state.environment.powerSaverLabel}")
    appendLine("Battery optimization: ${state.environment.batteryOptimizationLabel}")
    appendLine("Network: ${state.environment.networkLabel}")
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
