package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider

@Composable
fun SegmentedProgressBar(
    progress: Float,
    fillColor: ColorProvider,
    trackColor: ColorProvider,
    availableWidth: Dp,
    modifier: GlanceModifier = GlanceModifier,
    height: Dp = 9.dp,
    gap: Dp = 6.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val showFill = clamped > 0f
    val showTrack = clamped < 1f
    val fillWidth = (availableWidth * clamped).coerceAtLeast(height)

    Row(
        modifier = modifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (showFill) {
            Box(
                modifier = GlanceModifier
                    .width(fillWidth)
                    .fillMaxHeight()
                    .cornerRadius(height / 2)
                    .background(fillColor),
            ) {}
        }
        if (showFill && showTrack) {
            Spacer(GlanceModifier.width(gap))
        }
        if (showTrack) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(height / 2)
                    .background(trackColor),
            ) {}
        }
    }
}
