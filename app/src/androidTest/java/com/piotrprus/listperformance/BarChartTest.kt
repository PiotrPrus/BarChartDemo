package com.piotrprus.listperformance

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BarChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkCanvasExistence() {
        val list = mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8))
        composeTestRule.setContent {
            BarChartCanvas(list = list.value, barSelected = {  })
        }
        composeTestRule.onNodeWithTag(testTag = "BarChart").assertExists()
    }

    @Test
    fun clickOnThirdElementOfList() {
        val list = mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8))
        val selectedItem = mutableStateOf(list.value.first())
        val distance = with(composeTestRule.density) { (12.dp + 20.dp.times(3)).toPx() }
        composeTestRule.setContent {
            BarChartCanvas(list = list.value, barSelected = { selectedItem.value = it })
        }
        composeTestRule.onNodeWithTag(testTag = "BarChart")
            .performGesture { click(position = Offset(distance, 1f)) }
        println("Item selected: ${selectedItem.value}")
        assertEquals(3, selectedItem.value)
    }

    @Test
    fun cancelSelectionOfThirdElement() {
        val list = mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8))
        val selectedItem = mutableStateOf(list.value.first())
        val distance = with(composeTestRule.density) { (12.dp + 20.dp.times(3).minus(1.dp)).toPx() }
        composeTestRule.setContent {
            BarChartCanvas(list = list.value, barSelected = { selectedItem.value = it })
        }
        composeTestRule.onNodeWithTag(testTag = "BarChart")
            .performGesture { down(Offset(distance, 1f)) }
            .performGesture { moveBy(Offset(distance, 0f)) }
            .performGesture { up() }
        println("Item selected: ${selectedItem.value}")
        assertEquals(1, selectedItem.value)
    }

    @Test
    fun selectFifthElementByLongClick() {
        val list = mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8))
        val selectedItem = mutableStateOf(list.value.first())
        val distance = with(composeTestRule.density) { (12.dp + 20.dp.times(5).minus(1.dp)).toPx() }
        composeTestRule.setContent {
            BarChartCanvas(list = list.value, barSelected = { selectedItem.value = it })
        }
        composeTestRule.onNodeWithTag(testTag = "BarChart")
            .performGesture { down(Offset(distance, 1f)) }
            .performGesture { moveBy(Offset(0f, distance)) }
            .performGesture { up() }
        println("Item selected: ${selectedItem.value}")
        assertEquals(5, selectedItem.value)
    }

}