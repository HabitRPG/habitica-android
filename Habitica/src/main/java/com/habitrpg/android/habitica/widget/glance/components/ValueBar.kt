package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
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

@Composable
fun ValueBar(
    title: String,
    value: Float,
    maxValue: Float,
    barColor: Color,
    iconResId: Int,
    showLabels: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    val progress = if (maxValue > 0f) (value / maxValue).coerceIn(0f, 1f) else 0f
    val thickness = if (showLabels) 10.dp else 12.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = if (showLabels) Alignment.Top else Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = title,
            modifier = GlanceModifier.size(18.dp),
        )
        Spacer(GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            ProgressBar(
                fillColor = ColorProvider(barColor),
                trackColor = WidgetColors.progressTrack,
                progress = progress,
                height = thickness,
            )
            if (showLabels) {
                Spacer(GlanceModifier.height(2.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = TextStyle(
                            color = WidgetColors.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Text(
                        text = "${value.toInt()}/${maxValue.toInt()}",
                        style = TextStyle(
                            color = WidgetColors.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}
