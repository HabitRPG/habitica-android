package com.habitrpg.android.habitica.widget.glance.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.material3.ColorProviders

@Composable
fun HabiticaWidgetTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ColorProviders(
                light = dynamicLightColorScheme(context),
                dark = dynamicDarkColorScheme(context),
            )
        } else {
            HabiticaWidgetColorScheme.colors
        }
    }
    GlanceTheme(colors = colors, content = content)
}
