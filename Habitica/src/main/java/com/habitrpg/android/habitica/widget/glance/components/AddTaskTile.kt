package com.habitrpg.android.habitica.widget.glance.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.unit.ColorProvider

@Composable
fun AddTaskTile(
    label: String,
    iconResId: Int,
    backgroundColor: ColorProvider,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier,
    iconTint: ColorProvider? = null,
    cornerRadius: Dp = 28.dp,
    iconSize: Dp = 36.dp,
) {
    Column(
        modifier = modifier
            .cornerRadius(cornerRadius)
            .background(backgroundColor)
            .padding(8.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(iconResId),
            contentDescription = label,
            modifier = GlanceModifier.size(iconSize),
            colorFilter = iconTint?.let { ColorFilter.tint(it) },
        )
    }
}
