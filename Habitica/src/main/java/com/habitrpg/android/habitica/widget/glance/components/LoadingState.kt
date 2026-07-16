package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors

@Composable
fun WidgetLoadingContent(
    modifier: GlanceModifier = GlanceModifier,
    backgroundColor: ColorProvider = WidgetColors.cardBackground,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .cornerRadius(17.5.dp)
            .background(backgroundColor),
        content = {},
    )
}
