package com.habitrpg.android.habitica.extensions

import android.os.Build
import android.view.View
import android.view.Window

fun Window.updateStatusBarColor(color: Int, isLight: Boolean) {
    statusBarColor = color
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isStatusBarContrastEnforced = true
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility = if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
    }
}
