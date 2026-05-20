package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R

private val SQUIGGLE_TILE_WIDTH: Dp = 25.dp

@Composable
fun SquiggleProgressBar(
    progress: Float,
    fillColor: ColorProvider,
    trackColor: ColorProvider,
    availableWidth: Dp,
    modifier: GlanceModifier = GlanceModifier,
    height: Dp = 12.dp,
    trackThickness: Dp = 3.dp,
    gap: Dp = 6.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val targetFillWidth = (availableWidth * clamped).coerceAtLeast(0.dp)
    val tileWidthPx = SQUIGGLE_TILE_WIDTH.value
    val tileCount = if (clamped <= 0f) 0 else {
        (targetFillWidth.value / tileWidthPx).toInt().coerceAtLeast(1)
    }
    val showTrack = clamped < 1f

    Row(
        modifier = modifier.fillMaxWidth().height(height),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        repeat(tileCount) {
            Image(
                provider = ImageProvider(R.drawable.widget_progress_squiggle_tile),
                contentDescription = null,
                modifier = GlanceModifier.width(SQUIGGLE_TILE_WIDTH).fillMaxHeight(),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(fillColor),
            )
        }
        if (showTrack) {
            if (tileCount > 0) {
                Spacer(GlanceModifier.width(gap))
            }
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(trackThickness)
                        .cornerRadius(trackThickness / 2)
                        .background(trackColor),
                ) {}
            }
        }
    }
}
