package com.habitrpg.android.habitica.extensions

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.getThemeColor
import kotlin.and
import kotlin.or

fun Window.updateStatusBarColor(
    color: Int,
    isLight: Boolean
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return
    }
    statusBarColor = color
    @Suppress("DEPRECATION")
    decorView.systemUiVisibility =
        if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
}

@Suppress("DEPRECATION")
fun Window.setNavigationBarDarkIcons(dark: Boolean) {
    when {
        Build.VERSION_CODES.R <= Build.VERSION.SDK_INT -> insetsController?.setSystemBarsAppearance(
            if (dark) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        )
        else -> decorView.systemUiVisibility = if (dark) {
            decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }
}
