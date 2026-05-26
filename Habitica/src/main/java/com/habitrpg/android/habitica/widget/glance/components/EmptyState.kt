package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun EmptyState(
    message: String,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.cardBackground,
    textColor: ColorProvider = WidgetColors.text,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(17.5.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_sparkles),
                contentDescription = null,
                modifier = GlanceModifier.size(40.dp),
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = message,
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
fun StartDayCard(
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.cardBackground,
    textColor: ColorProvider = WidgetColors.text,
    iconTint: ColorProvider? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(8.dp),
        ) {
            Image(
                provider = ImageProvider(R.drawable.widget_start_day),
                contentDescription = stringRes(R.string.widget_start_day),
                modifier = GlanceModifier.size(36.dp),
                colorFilter = iconTint?.let { ColorFilter.tint(it) },
            )
            Spacer(GlanceModifier.height(12.dp))
            Text(
                text = stringRes(R.string.widget_start_day),
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
