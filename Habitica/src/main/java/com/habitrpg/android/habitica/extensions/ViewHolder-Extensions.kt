package com.habitrpg.android.habitica.extensions

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.view.View

fun <T : View> bindView(container: View, @IdRes res: Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { container.findViewById<T>(res) }
}

fun <T : View> bindOptionalView(container: View?, @IdRes res: Int) : Lazy<T?> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { container?.findViewById<T>(res) }
}

fun bindColor(context: Context, @ColorRes res: Int) : Lazy<Int> {
    @Suppress("UNCHECKED_CAST")
    return lazy(LazyThreadSafetyMode.NONE) { ContextCompat.getColor(context, res) }
}