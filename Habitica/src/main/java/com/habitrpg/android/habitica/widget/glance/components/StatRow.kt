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
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

private val MaterialYouEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
private val BAR_HEIGHT = 9.dp
private val ICON_SIZE = 24.dp

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
    labelTextColor: ColorProvider = WidgetColors.text,
    iconSize: Dp = ICON_SIZE,
    valueColumnWidth: Dp = 0.dp,
    barHeight: Dp = BAR_HEIGHT,
) {
    val progress = if (maxValue > 0f) (value / maxValue).coerceIn(0f, 1f) else 0f
    val fillColor = ColorProvider(barColor)
    val trackColor = if (MaterialYouEnabled) GlanceTheme.colors.outline else WidgetColors.progressTrack
    val rowAlignment = if (mode == StatRowMode.LabelStackedValue) {
        Alignment.Top
    } else {
        Alignment.CenterVertically
    }
    val barCenterOffset = (iconSize - barHeight) / 2
    val isInlineMode = mode == StatRowMode.InlineValueWithLabel ||
        mode == StatRowMode.InlineValueMaxWithLabel
    val rowModifier = if (isInlineMode) {
        modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp)
    } else {
        modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = rowAlignment,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = label,
            modifier = GlanceModifier.size(iconSize),
        )
        Spacer(GlanceModifier.width(8.dp))

        when (mode) {
            StatRowMode.BarOnly -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SegmentedProgressBar(progress = progress, fillColor = fillColor, trackColor = trackColor, availableWidth = barAvailableWidth, height = barHeight)
                }
            }
            StatRowMode.LabelStackedValue -> {
                Column(modifier = GlanceModifier.defaultWeight().padding(top = barCenterOffset)) {
                    SegmentedProgressBar(progress = progress, fillColor = fillColor, trackColor = trackColor, availableWidth = barAvailableWidth, height = barHeight)
                    Spacer(GlanceModifier.height(2.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = label,
                            style = labelStyle(labelTextColor),
                            modifier = GlanceModifier.defaultWeight(),
                        )
                        Text(text = valueText, style = labelStyle(labelTextColor))
                        Text(text = " / ", style = separatorStyle(labelTextColor))
                        Text(text = maxText, style = labelStyle(labelTextColor))
                    }
                }
            }
            StatRowMode.InlineValueWithLabel,
            StatRowMode.InlineValueMaxWithLabel -> {
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SegmentedProgressBar(progress = progress, fillColor = fillColor, trackColor = trackColor, availableWidth = barAvailableWidth, height = barHeight)
                }
                Spacer(GlanceModifier.width(4.dp))
                val valueModifier = if (valueColumnWidth > 0.dp) {
                    GlanceModifier.width(valueColumnWidth)
                } else {
                    GlanceModifier
                }
                Text(
                    text = inlineValueText(mode, valueText, maxText, label),
                    style = inlineValueStyle(labelTextColor),
                    maxLines = 1,
                    modifier = valueModifier,
                )
            }
        }
    }
}

private fun labelStyle(color: ColorProvider) = TextStyle(
    color = color,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
)

private fun separatorStyle(color: ColorProvider) = TextStyle(
    color = color,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
)

private fun inlineValueStyle(color: ColorProvider) = TextStyle(
    color = color,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.End,
)

fun inlineValueText(mode: StatRowMode, valueText: String, maxText: String, label: String): String =
    when (mode) {
        StatRowMode.InlineValueMaxWithLabel -> "$valueText / $maxText $label"
        else -> "$valueText $label"
    }

private fun estimateInlineCharWidthDp(c: Char): Float = when {
    c.isDigit() -> 8.5f
    c == ' ' -> 4f
    c == '/' -> 6f
    else -> 9f
}

fun inlineValueColumnWidth(texts: List<String>): Dp {
    val widest = texts.maxOfOrNull { text ->
        text.fold(0f) { acc, c -> acc + estimateInlineCharWidthDp(c) }
    } ?: 0f
    return widest.dp + 4.dp
}
