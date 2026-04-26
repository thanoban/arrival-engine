package com.nearwake.feature.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionsViewModelTest {

    @Test
    fun `missing notifications and location requires action`() {
        val state = PermissionSnapshot(
            notificationGranted = false,
            fineLocationGranted = false,
            backgroundLocationGranted = false,
            activityRecognitionGranted = false,
            notificationsRequestable = true,
            backgroundLocationRelevant = true,
            activityRecognitionRelevant = true,
        ).toUiState()

        assertEquals(PermissionReadiness.ACTION_NEEDED, state.readiness)
        assertEquals(PermissionPromptAction.REQUEST_CORE, state.primaryAction)
        assertEquals("Action needed", state.readinessLabel)
    }

    @Test
    fun `missing background only is limited`() {
        val state = PermissionSnapshot(
            notificationGranted = true,
            fineLocationGranted = true,
            backgroundLocationGranted = false,
            activityRecognitionGranted = true,
            notificationsRequestable = true,
            backgroundLocationRelevant = true,
            activityRecognitionRelevant = true,
        ).toUiState()

        assertEquals(PermissionReadiness.LIMITED, state.readiness)
        assertEquals(PermissionPromptAction.REQUEST_BACKGROUND, state.primaryAction)
        assertEquals("Limited", state.readinessLabel)
    }

    @Test
    fun `all relevant permissions granted is ready`() {
        val state = PermissionSnapshot(
            notificationGranted = true,
            fineLocationGranted = true,
            backgroundLocationGranted = true,
            activityRecognitionGranted = true,
            notificationsRequestable = true,
            backgroundLocationRelevant = true,
            activityRecognitionRelevant = true,
        ).toUiState()

        assertEquals(PermissionReadiness.READY, state.readiness)
        assertEquals(PermissionPromptAction.REFRESH, state.primaryAction)
        assertEquals("Ready", state.readinessLabel)
    }

    @Test
    fun `non requestable notification block opens settings`() {
        val state = PermissionSnapshot(
            notificationGranted = false,
            fineLocationGranted = true,
            backgroundLocationGranted = true,
            activityRecognitionGranted = true,
            notificationsRequestable = false,
            backgroundLocationRelevant = true,
            activityRecognitionRelevant = true,
        ).toUiState()

        assertEquals(PermissionPromptAction.OPEN_SETTINGS, state.primaryAction)
    }
}
