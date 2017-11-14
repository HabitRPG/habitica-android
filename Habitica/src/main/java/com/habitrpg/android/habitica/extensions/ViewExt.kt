package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.support.annotation.IdRes
import android.view.View


fun View.setScaledPadding(context: Context?, left: Int, top: Int, right: Int, bottom: Int) {
    this.setPadding(left.dpToPx(context), top.dpToPx(context), right.dpToPx(context), bottom.dpToPx(context))
}

fun <T : View> View.bindView(@IdRes res: Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }
}