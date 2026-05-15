package com.nearwake.core.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.nearwake.core.designsystem.NearWakeTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NearWakeStatusTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun stateChip_exposesStatusDescription_withoutClickRole() {
        composeRule.setContent {
            NearWakeTheme {
                NearWakeStateChip(
                    label = "High confidence",
                    state = NearWakeChipState.Safe,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("High confidence. Safe")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))
    }

    @Test
    fun selectableChip_exposesButtonRole_andSelectionState() {
        composeRule.setContent {
            NearWakeTheme {
                NearWakeSelectableChip(
                    selected = true,
                    label = "Sleep",
                    onClick = {},
                )
            }
        }

        composeRule
            .onNodeWithText("Sleep")
            .assertHasClickAction()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
    }

    @Test
    fun selectableChip_invokesClick() {
        var clicked = false

        composeRule.setContent {
            NearWakeTheme {
                NearWakeSelectableChip(
                    selected = false,
                    label = "Active",
                    onClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Active").performClick()

        composeRule.runOnIdle {
            assertTrue(clicked)
        }
    }
}
