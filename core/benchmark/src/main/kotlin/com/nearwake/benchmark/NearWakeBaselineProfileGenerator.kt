package com.nearwake.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NearWakeBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = TARGET_PACKAGE,
    ) {
        pressHome()
        startActivityAndWait()
        device.wait(Until.hasObject(By.text("NearWake")), UI_TIMEOUT_MS)

        // Capture the core cold-start journey, then open the trip setup funnel entry.
        device.findObject(By.text("Continue"))?.click()
        device.wait(Until.hasObject(By.text("Choose destination")), UI_TIMEOUT_MS)
        device.findObject(By.text("Choose destination"))?.click()
        device.wait(Until.hasObject(By.text("Pick a destination")), UI_TIMEOUT_MS)
    }

    private companion object {
        const val TARGET_PACKAGE = "com.nearwake.app"
        const val UI_TIMEOUT_MS = 5_000L
    }
}
