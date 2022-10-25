package com.piotrprus.listperformance

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piotrprus.listperformance.ui.theme.ListPerformanceTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListPerformanceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val list = remember {
                        mutableStateOf(List(100) { Random.nextInt(0, 100) })
                    }
                    val state = rememberScrollState()
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(state = state)
                    ) {
                        val selectedItem = remember { mutableStateOf(list.value.first()) }
                        Text(text = "Selected value: ${selectedItem.value}")
                        BarChartCanvas(list.value) { selectedItem.value = it }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChartCanvas(list: List<Int>, barSelected: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(150.dp)
            .testTag("BarChart")
            .horizontalScroll(rememberScrollState())
    ) {
        val density = LocalDensity.current
        val skyBlue400 = Color(0xff57A5FF)
        val horizontalPadding = with(density) { 12.dp.toPx() }
        val distance = with(density) { 26.dp.toPx() }
        val calculatedWidth =
            with(density) { (distance.times(list.size) + horizontalPadding.times(2)).toDp() }
        val barWidth = with(density) { 12.dp.toPx() }
        val selectionWidth = with(density) { 20.dp.toPx() }
        val smallPadding = with(density) { 4.dp.toPx() }
        val textSize = with(density) { 10.sp.toPx() }
        val cornerRadius = with(density) { 4.dp.toPx() }
        val labelSectionHeight = smallPadding.times(2) + textSize
        val paint = Paint().apply {
            color = 0xffff47586B.toInt()
            textAlign = Paint.Align.CENTER
            this.textSize = textSize
        }
        val barAreas = list.mapIndexed { index, i ->
            BarArea(
                index = index,
                value = i,
                xStart = horizontalPadding + distance.times(index) - distance.div(2),
                xEnd = horizontalPadding + distance.times(index) + distance.div(2)
            )
        }
        var selectedPosition by remember { mutableStateOf(barAreas.first().xStart.plus(1f)) }
        var tempPosition by remember { mutableStateOf(-1000f) }
        val selectedBar by remember(selectedPosition, barAreas) {
            derivedStateOf {
                barAreas.find { it.xStart < selectedPosition && selectedPosition < it.xEnd }
            }
        }
        val tempBar by remember(tempPosition, barAreas) {
            derivedStateOf {
                barAreas.find { it.xStart < tempPosition && tempPosition < it.xEnd }
            }
        }

        val scope = rememberCoroutineScope()
        val animatable = remember { Animatable(1f) }
        val tempAnimatable = remember { Animatable(0f) }

        val textMeasurer = rememberTextMeasurer()

        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .width(calculatedWidth)
                .tapOrPress(
                    onStart = { position ->
                        scope.launch {
                            selectedBar?.let { selected ->
                                if (position in selected.xStart..selected.xEnd) {
                                    // click in selected area - do nothing
                                } else {
                                    tempPosition = position
                                    scope.launch {
                                        tempAnimatable.snapTo(0f)
                                        tempAnimatable.animateTo(1f, animationSpec = tween(300))
                                    }
                                }

                            }
                        }
                    },
                    onCancel = { position ->
                        tempPosition = -Int.MAX_VALUE.toFloat()
                        scope.launch {
                            tempAnimatable.animateTo(0f)
                        }
                    },
                    onCompleted = {
                        val currentSelected = selectedBar
                        scope.launch {
                            selectedPosition = it
                            animatable.snapTo(tempAnimatable.value)
                            selectedBar?.value?.let { value ->
                                barSelected(value)
                            }
                            async {
                                animatable.animateTo(
                                    1f,
                                    animationSpec = tween(
                                        300
                                            .times(1f - tempAnimatable.value)
                                            .roundToInt()
                                    )
                                )
                            }
                            async {
                                tempAnimatable.snapTo(0f)
                                currentSelected?.let {
                                    tempPosition = currentSelected.xStart.plus(1f)
                                    tempAnimatable.snapTo(1f)
                                    tempAnimatable.animateTo(0f, tween(300))
                                }
                            }
                        }
                    }
                )
        ) {
            val lineDistance = size.height.minus(smallPadding.times(2)).div(4)
            repeat(5) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, smallPadding.plus(it.times(lineDistance))),
                    end = Offset(size.width, smallPadding.plus(it.times(lineDistance)))
                )
            }
            val scale = calculateScale((size.height - smallPadding).roundToInt(), list)
            val chartAreaBottom = size.height - labelSectionHeight
            barAreas.forEachIndexed { index, item ->
                val barHeight = item.value.times(scale).toFloat()
                drawRoundRect(
                    color = skyBlue400,
                    topLeft = Offset(
                        x = horizontalPadding + distance.times(index) - barWidth.div(2),
                        y = size.height - barHeight - smallPadding
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadius)
                )
                val textPositionY = chartAreaBottom - barHeight - smallPadding - 10f
                val textPositionX = horizontalPadding + distance.times(index) - barWidth / 2
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${item.value}",
                    topLeft = Offset(textPositionX, textPositionY)
                )
            }
            if (selectedBar != null) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            skyBlue400.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(
                        x = horizontalPadding + distance.times(selectedBar!!.index) - selectionWidth.div(
                            2
                        ), y = size.height + smallPadding - size.height.times(animatable.value)
                    ),
                    size = Size(selectionWidth, size.height.minus(smallPadding.times(2))),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }
            if (tempBar != null) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            skyBlue400.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(
                        x = horizontalPadding + distance.times(tempBar!!.index) - selectionWidth.div(
                            2
                        ), y = size.height + smallPadding - size.height.times(tempAnimatable.value)
                    ),
                    size = Size(selectionWidth, size.height.minus(smallPadding.times(2))),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }
        }
    }
}

fun calculateScale(viewHeightPx: Int, values: List<Int>): Double {
    return values.maxOrNull()?.let { max ->
        viewHeightPx.times(0.8).div(max)
    } ?: 1.0
}

fun Modifier.tapOrPress(
    onStart: (offsetX: Float) -> Unit,
    onCancel: (offsetX: Float) -> Unit,
    onCompleted: (offsetX: Float) -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.pointerInput(interactionSource) {
        forEachGesture {
            coroutineScope {
                awaitPointerEventScope {
                    val tap = awaitFirstDown()
                        .also { it.consumeDownChange() }
                    onStart(tap.position.x)
                    val up = waitForUpOrCancellation()
                    if (up == null) {
                        onCancel(tap.position.x)
                    } else {
                        up.consumeDownChange()
                        onCompleted(tap.position.x)
                    }
                }
            }
        }
    }
}