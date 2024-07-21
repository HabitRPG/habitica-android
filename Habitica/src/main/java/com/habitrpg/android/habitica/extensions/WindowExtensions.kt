package com.habitrpg.android.habitica.extensions

import android.os.Build
import android.view.View
import android.view.Window
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensionsCommon.getThemeColor

fun Window.updateStatusBarColor(
    color: Int,
    isLight: Boolean,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        statusBarColor = color
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility =
            if (isLight) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else View.SYSTEM_UI_FLAG_VISIBLE
    } else {
        statusBarColor = context.getThemeColor(R.attr.colorPrimaryDark)
    }
}
