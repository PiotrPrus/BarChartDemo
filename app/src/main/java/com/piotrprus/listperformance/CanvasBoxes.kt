package com.piotrprus.listperformance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CanvasBoxes() {
    Column {
        val selectedBox = remember { mutableStateOf(0) }
        Text(modifier = Modifier.semantics { testTag = "title" }, text = "Box selected: ${selectedBox.value}")
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .semantics { testTag = "BoxCanvas" }
                .tapOrPress(onCompleted = { positionX ->
                    if (positionX < constraints.minWidth.div(2)) {
                        selectedBox.value = 1
                    } else {
                        selectedBox.value = 2
                    }
                }, onCancel = { selectedBox.value = 0 }, onStart = {}), onDraw = {
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(0f, 0f),
                    size = Size(this.size.width.div(2), this.size.height)
                )
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(this.size.width.div(2), 0f),
                    size = Size(this.size.width.div(2), this.size.height)
                )
            })
        }
    }
}

@Preview
@Composable
fun PreviewCanvasBoxes() {
    Box(modifier = Modifier.size(300.dp, 150.dp)) {
        CanvasBoxes()
    }
}