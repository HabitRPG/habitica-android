package com.habitrpg.android.habitica.widget.glance.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
private val BAR_HEIGHT = 6.dp
private val ICON_SIZE = 20.dp

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
    @Suppress("UNUSED_PARAMETER") barAvailableWidth: Dp,
    modifier: GlanceModifier = GlanceModifier,
    labelTextColor: ColorProvider = WidgetColors.text,
) {
    val progress = if (maxValue > 0f) (value / maxValue).coerceIn(0f, 1f) else 0f
    val fillColor = ColorProvider(barColor)
    val trackColor = if (MaterialYouEnabled) GlanceTheme.colors.outline else WidgetColors.progressTrack
    val rowAlignment = if (mode == StatRowMode.LabelStackedValue) {
        Alignment.Top
    } else {
        Alignment.CenterVertically
    }
    val barCenterOffset = (ICON_SIZE - BAR_HEIGHT) / 2

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = rowAlignment,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = label,
            modifier = GlanceModifier.size(ICON_SIZE),
        )
        Spacer(GlanceModifier.width(8.dp))

        when (mode) {
            StatRowMode.BarOnly -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    StatProgressBar(progress, fillColor, trackColor)
                }
            }
            StatRowMode.LabelStackedValue -> {
                Column(modifier = GlanceModifier.defaultWeight().padding(top = barCenterOffset)) {
                    StatProgressBar(progress, fillColor, trackColor)
                    Spacer(GlanceModifier.height(2.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = label,
                            style = labelStyle(labelTextColor),
                            modifier = GlanceModifier.defaultWeight(),
                        )
                        Text(
                            text = "$valueText / $maxText",
                            style = labelStyle(labelTextColor),
                        )
                    }
                }
            }
            StatRowMode.InlineValueWithLabel -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    StatProgressBar(progress, fillColor, trackColor)
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "$valueText $label",
                    style = labelStyle(labelTextColor),
                )
            }
            StatRowMode.InlineValueMaxWithLabel -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    StatProgressBar(progress, fillColor, trackColor)
                }
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = "$valueText / $maxText $label",
                    style = labelStyle(labelTextColor),
                )
            }
        }
    }
}

@Composable
private fun StatProgressBar(
    progress: Float,
    fillColor: ColorProvider,
    trackColor: ColorProvider,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(BAR_HEIGHT)
            .cornerRadius(BAR_HEIGHT / 2),
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = GlanceModifier.fillMaxWidth().height(BAR_HEIGHT),
            color = fillColor,
            backgroundColor = trackColor,
        )
    }
}

private fun labelStyle(color: ColorProvider) = TextStyle(
    color = color,
    fontSize = 11.sp,
    fontWeight = FontWeight.Medium,
)
