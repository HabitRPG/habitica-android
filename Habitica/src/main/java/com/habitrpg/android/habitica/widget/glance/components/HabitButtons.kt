package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R

@Composable
fun HabitButtonBar(
    showUp: Boolean,
    showDown: Boolean,
    barColor: Color,
    circleColor: Color,
    isTall: Boolean,
    onUpClick: Action,
    onDownClick: Action,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(modifier = modifier) {
        Image(
            provider = ImageProvider(R.drawable.widget_habit_bar_top),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(ColorProvider(barColor)),
        )
        when {
            showUp && showDown && isTall -> StackedDirections(
                circleColor = circleColor,
                onUpClick = onUpClick,
                onDownClick = onDownClick,
            )
            showUp && showDown -> SplitDirections(
                circleColor = circleColor,
                onUpClick = onUpClick,
                onDownClick = onDownClick,
            )
            showUp -> SingleDirection(
                glyphRes = R.drawable.habit_plus,
                description = "Score up",
                circleColor = circleColor,
                onClick = onUpClick,
            )
            showDown -> SingleDirection(
                glyphRes = R.drawable.habit_minus,
                description = "Score down",
                circleColor = circleColor,
                onClick = onDownClick,
            )
        }
    }
}

@Composable
private fun SplitDirections(
    circleColor: Color,
    onUpClick: Action,
    onDownClick: Action,
) {
    Row(modifier = GlanceModifier.fillMaxSize()) {
        DirectionRegion(
            glyphRes = R.drawable.habit_minus,
            description = "Score down",
            circleColor = circleColor,
            onClick = onDownClick,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
        )
        DirectionRegion(
            glyphRes = R.drawable.habit_plus,
            description = "Score up",
            circleColor = circleColor,
            onClick = onUpClick,
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
        )
    }
}

@Composable
private fun StackedDirections(
    circleColor: Color,
    onUpClick: Action,
    onDownClick: Action,
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        DirectionRegion(
            glyphRes = R.drawable.habit_plus,
            description = "Score up",
            circleColor = circleColor,
            onClick = onUpClick,
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
        )
        DirectionRegion(
            glyphRes = R.drawable.habit_minus,
            description = "Score down",
            circleColor = circleColor,
            onClick = onDownClick,
            modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
        )
    }
}

@Composable
private fun SingleDirection(
    glyphRes: Int,
    description: String,
    circleColor: Color,
    onClick: Action,
) {
    DirectionRegion(
        glyphRes = glyphRes,
        description = description,
        circleColor = circleColor,
        onClick = onClick,
        modifier = GlanceModifier.fillMaxSize(),
    )
}

@Composable
private fun DirectionRegion(
    glyphRes: Int,
    description: String,
    circleColor: Color,
    onClick: Action,
    modifier: GlanceModifier,
    circleSize: Dp = 32.dp,
    glyphSize: Dp = 16.dp,
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier
                .size(circleSize)
                .cornerRadius(circleSize / 2)
                .background(ColorProvider(circleColor)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(glyphRes),
                contentDescription = description,
                modifier = GlanceModifier.size(glyphSize),
                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
            )
        }
    }
}
