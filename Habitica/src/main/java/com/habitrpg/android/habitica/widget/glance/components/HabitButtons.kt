package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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

@Composable
fun HabitButtonsRow(
    text: String,
    showUp: Boolean,
    showDown: Boolean,
    upColor: Color,
    downColor: Color,
    textColor: ColorProvider,
    onUpClick: Action,
    onDownClick: Action,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 2,
            modifier = GlanceModifier.defaultWeight(),
        )
        Spacer(GlanceModifier.width(8.dp))
        if (showUp) {
            HabitDirectionButton(label = "+", bgColor = upColor, onClick = onUpClick)
        }
        if (showUp && showDown) {
            Spacer(GlanceModifier.width(8.dp))
        }
        if (showDown) {
            HabitDirectionButton(label = "−", bgColor = downColor, onClick = onDownClick)
        }
    }
}

@Composable
private fun HabitDirectionButton(label: String, bgColor: Color, onClick: Action) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .background(ColorProvider(bgColor))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
