package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun DailyCountGauge(
    completedCount: Int,
    totalCount: Int,
    showRemaining: Boolean,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier,
) {
    val displayCount = if (showRemaining) (totalCount - completedCount) else completedCount
    val midLabel = if (showRemaining) "Dailies left" else "Dailies done"
    val bottomLabel = if (showRemaining) "$completedCount done" else "${totalCount - completedCount} left to do"

    val barColors = listOf(WidgetBarColors.red, WidgetBarColors.yellow, WidgetBarColors.blue)
    val progress = if (totalCount > 0) (completedCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f
    val barColorIndex = if (totalCount > 0) {
        ((progress * barColors.size).toInt()).coerceIn(0, barColors.size - 1)
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = displayCount.toString(),
            style = TextStyle(
                color = WidgetColors.dailiesPurple,
                fontSize = 50.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = midLabel,
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.height(10.dp))
        ProgressBar(
            fillColor = ColorProvider(barColors[barColorIndex]),
            trackColor = WidgetColors.progressTrack,
            progress = progress,
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = bottomLabel,
            style = TextStyle(
                color = WidgetColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}

@Composable
fun ProgressBar(
    fillColor: ColorProvider,
    trackColor: ColorProvider,
    progress: Float,
    height: androidx.compose.ui.unit.Dp = 12.dp,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(height)
            .cornerRadius(height / 2),
    ) {
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = GlanceModifier.fillMaxWidth().height(height),
            color = fillColor,
            backgroundColor = trackColor,
        )
    }
}
