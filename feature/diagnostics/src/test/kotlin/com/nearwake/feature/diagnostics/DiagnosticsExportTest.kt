package com.nearwake.feature.diagnostics
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsExportTest {

    @Test
    fun `export includes service state geofences and events`() {
        val export = buildDiagnosticsExport(
            DiagnosticsUiState(
                buildInfo = DiagnosticsBuildInfoUiModel(
                    appVersionLabel = "0.1.0 (1)",
                    buildTypeLabel = "Debug",
                    deviceLabel = "Google Pixel 8 · Android 15",
                ),
                permissions = DiagnosticsPermissionSummaryUiModel(
                    readinessLabel = "Limited",
                    summary = "Trips can run, but screen-off reliability or power-aware behavior is reduced.",
                    statuses = listOf(
                        DiagnosticsPermissionStatusUiModel("Notifications", granted = true),
                        DiagnosticsPermissionStatusUiModel("Precise location", granted = true),
                        DiagnosticsPermissionStatusUiModel("Background location", granted = false),
                    ),
                ),
                environment = DiagnosticsEnvironmentUiModel(
                    powerSaverLabel = "Off",
                    batteryOptimizationLabel = "Standard battery optimization",
                    networkLabel = "Connected",
                ),
                stateLabel = "APPROACH",
                registeredGeofences = listOf("dest-1", "dest-2"),
                recentEvents = listOf(
                    DiagnosticsEventUiModel(
                        label = "Alert Fired",
                        summary = "ARRIVAL alert - STANDARD intensity",
                        tripPrefix = "abc12345",
                        recordedAtLabel = "2026-04-27 11:45:00",
                    ),
                ),
            ),
        )

        assertTrue(export.contains("App version: 0.1.0 (1)"))
        assertTrue(export.contains("Build type: Debug"))
        assertTrue(export.contains("Device: Google Pixel 8 · Android 15"))
        assertTrue(export.contains("Permission readiness: Limited"))
        assertTrue(export.contains("Background location: Needed"))
        assertTrue(export.contains("Power saver: Off"))
        assertTrue(export.contains("Battery optimization: Standard battery optimization"))
        assertTrue(export.contains("Network: Connected"))
        assertTrue(export.contains("Service state: APPROACH"))
        assertTrue(export.contains("- dest-1"))
        assertTrue(export.contains("2026-04-27 11:45:00 Alert Fired [abc12345]"))
        assertTrue(export.contains("ARRIVAL alert - STANDARD intensity"))
    }

    @Test
    fun `export shows empty markers when no data exists`() {
        val export = buildDiagnosticsExport(DiagnosticsUiState())

        assertTrue(export.contains("App version: "))
        assertTrue(export.contains("Service state: No active trip"))
        assertTrue(export.contains("- none"))
        assertTrue(export.contains("- none recorded"))
    }
}
