package com.piotrprus.listperformance

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.piotrprus.listperformance.ui.theme.ListPerformanceTheme
import org.junit.Rule
import org.junit.Test

class CanvasBoxesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickOnFirstPartOfCanvasAndCheckIfNumberTwoIsDisplayedInFiled() {
        composeTestRule.setContent {
            ListPerformanceTheme {
                CanvasBoxes()
            }
        }
        composeTestRule.onNodeWithTag(testTag = "BoxCanvas")
            .performGesture { click(position = Offset(5f, 5f)) }
        // This passes
        composeTestRule.onNodeWithText("Box selected: 1").assertExists()
        // This fails
        composeTestRule.onNodeWithTag(testTag = "title").assertTextContains(value = "1", substring = true)
    }
}