package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

enum class StatRowMode {
    BarOnly,
    LabelStackedValue,
    InlineValueWithLabel,
    InlineValueMaxWithLabel,
}

@Composable
fun StatRow(
    label: String,
    value: Float,
    maxValue: Float,
    valueText: String,
    maxText: String,
    barColor: Color,
    iconResId: Int,
    mode: StatRowMode,
    barAvailableWidth: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val progress = if (maxValue > 0f) (value / maxValue).coerceIn(0f, 1f) else 0f
    val fillColor = ColorProvider(barColor)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = label,
            modifier = GlanceModifier.size(16.dp),
        )
        Spacer(GlanceModifier.width(8.dp))

        when (mode) {
            StatRowMode.BarOnly -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SquiggleProgressBar(
                        progress = progress,
                        fillColor = fillColor,
                        trackColor = WidgetColors.progressTrack,
                        availableWidth = barAvailableWidth,
                        height = 10.dp,
                    )
                }
            }
            StatRowMode.LabelStackedValue -> {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    SquiggleProgressBar(
                        progress = progress,
                        fillColor = fillColor,
                        trackColor = WidgetColors.progressTrack,
                        availableWidth = barAvailableWidth,
                        height = 10.dp,
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = label,
                            style = labelStyle(),
                            modifier = GlanceModifier.defaultWeight(),
                        )
                        Text(
                            text = "$valueText / $maxText",
                            style = labelStyle(),
                        )
                    }
                }
            }
            StatRowMode.InlineValueWithLabel -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SquiggleProgressBar(
                        progress = progress,
                        fillColor = fillColor,
                        trackColor = WidgetColors.progressTrack,
                        availableWidth = barAvailableWidth,
                        height = 10.dp,
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "$valueText $label",
                    style = labelStyle(),
                )
            }
            StatRowMode.InlineValueMaxWithLabel -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SquiggleProgressBar(
                        progress = progress,
                        fillColor = fillColor,
                        trackColor = WidgetColors.progressTrack,
                        availableWidth = barAvailableWidth,
                        height = 10.dp,
                    )
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "$valueText / $maxText $label",
                    style = labelStyle(),
                )
            }
        }
    }
}

private fun labelStyle() = TextStyle(
    color = WidgetColors.text,
    fontSize = 11.sp,
    fontWeight = FontWeight.Medium,
)
