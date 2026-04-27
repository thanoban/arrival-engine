package com.nearwake.feature.diagnostics

import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsExportTest {

    @Test
    fun `export includes service state geofences and events`() {
        val export = buildDiagnosticsExport(
            DiagnosticsUiState(
                stateLabel = "APPROACH",
                registeredGeofences = listOf("dest-1", "dest-2"),
                recentEvents = listOf(
                    DiagnosticsEventUiModel(
                        label = "Alert Fired",
                        summary = "ARRIVAL alert - STANDARD intensity",
                        tripPrefix = "abc12345",
                    ),
                ),
            ),
        )

        assertTrue(export.contains("Service state: APPROACH"))
        assertTrue(export.contains("- dest-1"))
        assertTrue(export.contains("Alert Fired [abc12345]"))
        assertTrue(export.contains("ARRIVAL alert - STANDARD intensity"))
    }

    @Test
    fun `export shows empty markers when no data exists`() {
        val export = buildDiagnosticsExport(DiagnosticsUiState())

        assertTrue(export.contains("Service state: No active trip"))
        assertTrue(export.contains("- none"))
        assertTrue(export.contains("- none recorded"))
    }
}
