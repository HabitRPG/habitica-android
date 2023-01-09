package com.habitrpg.android.habitica.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy

fun flowLayoutMeasurePolicy(spacing: Int = 0) = MeasurePolicy{ measurables,constraints ->
    layout(constraints.maxWidth,constraints.maxHeight){
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        var yPos = 0
        var xPos = 0
        var maxY = 0
        placeables.forEach { placeable ->
            if (xPos != 0) {
                xPos += spacing
            }
            if (xPos + placeable.width >
                constraints.maxWidth
            ) {
                xPos = 0
                yPos += maxY + spacing
                maxY = 0
            }
            placeable.placeRelative(
                x = xPos,
                y = yPos
            )
            xPos += placeable.width
            if (maxY < placeable.height) {
                maxY = placeable.height
            }
        }
    }
}

@Composable
fun FlowLayout(
    modifier: Modifier = Modifier,
    spacing: Int = 0,
    content: @Composable () -> Unit,
){
    val measurePolicy = flowLayoutMeasurePolicy(spacing)
    Layout(measurePolicy = measurePolicy,
        content = content,
        modifier = modifier )
}