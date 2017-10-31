package com.habitrpg.android.habitica.extensions

import android.support.annotation.IdRes
import android.view.View

fun <T : View> bindView(container: View, @IdRes res: Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { container.findViewById<T>(res) }
}