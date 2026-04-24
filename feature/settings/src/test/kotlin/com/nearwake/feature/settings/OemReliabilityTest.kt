package com.nearwake.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OemReliabilityTest {

    @Test
    fun `background monitoring disabled returns degraded status`() {
        val guidance = oemReliabilityGuidance(
            manufacturer = "Samsung",
            backgroundMonitoringEnabled = false,
        )

        assertEquals("Degraded", guidance.statusLabel)
        assertEquals(OemReliabilityTone.Degraded, guidance.statusTone)
    }

    @Test
    fun `samsung guidance includes sleeping apps instruction`() {
        val guidance = oemReliabilityGuidance(
            manufacturer = "Samsung",
            backgroundMonitoringEnabled = true,
        )

        assertEquals("Samsung", guidance.manufacturerLabel)
        assertEquals(OemReliabilityTone.Review, guidance.statusTone)
        assertTrue(guidance.steps.any { it.contains("Sleeping apps") })
    }

    @Test
    fun `pixel guidance stays stable`() {
        val guidance = oemReliabilityGuidance(
            manufacturer = "Google",
            backgroundMonitoringEnabled = true,
        )

        assertEquals("Standard Android", guidance.statusLabel)
        assertEquals(OemReliabilityTone.Stable, guidance.statusTone)
    }
}
