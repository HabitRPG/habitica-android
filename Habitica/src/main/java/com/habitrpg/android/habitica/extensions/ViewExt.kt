package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.IdRes
import android.view.View


fun View.setScaledPadding(context: Context?, left: Int, top: Int, right: Int, bottom: Int) {
    this.setPadding(left.dpToPx(context), top.dpToPx(context), right.dpToPx(context), bottom.dpToPx(context))
}

var <T : View> T.backgroundCompat: Drawable?
get() {
    return background
}
set(value) {
    if (value == null) {
        return
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background = value
    } else {
        @Suppress("DEPRECATION")
        setBackgroundDrawable(value)
    }
}