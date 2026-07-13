package com.habitrpg.android.habitica.widget.glance.theme

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme

@Composable
fun HabiticaWidgetTheme(
    content: @Composable () -> Unit,
) {
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceTheme.colors
    } else {
        HabiticaWidgetColorScheme.colors
    }
    GlanceTheme(colors = colors, content = content)
}
