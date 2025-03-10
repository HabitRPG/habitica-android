package com.habitrpg.android.habitica.extensions

import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun consumeWindowInsetsAbove30(insets: WindowInsetsCompat): WindowInsetsCompat {
    if (Build.VERSION.SDK_INT >= 30) return WindowInsetsCompat.CONSUMED
    return insets
}

fun applyScrollContentWindowInsets(view: View,
                                   applyTop: Boolean = false,
                                   applyBottom: Boolean = true,
                                   applyLeft: Boolean = true,
                                   applyRight: Boolean = true) {
    val topPadding = view.paddingTop
    val leftPadding = view.paddingLeft
    val rightPadding = view.paddingRight
    val bottomPadding = view.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
        val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout())
        val top = (if (applyTop) insets.top else 0) + topPadding
        val bottom = (if (applyBottom) insets.bottom else 0) + bottomPadding
        if (v.layoutParams.height > 0 && v.layoutParams.height < top + bottom) {
            v.layoutParams.height += top + bottom
        }
        v.updatePadding(
            top = top,
            left = (if (applyLeft) insets.left else 0) + leftPadding,
            right = (if (applyRight) insets.right else 0) + rightPadding,
            bottom = bottom)
        consumeWindowInsetsAbove30(windowInsets)
    }
}
