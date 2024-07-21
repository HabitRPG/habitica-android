package com.habitrpg.common.habitica.extensionsCommon

import android.content.Context

fun Int.dpToPx(context: Context?): Int {
    val displayMetrics = context?.resources?.displayMetrics
    return ((this * (displayMetrics?.density ?: 1.0f)) + 0.5).toInt()
}

fun Float.dpToPx(context: Context?): Float {
    val displayMetrics = context?.resources?.displayMetrics
    return ((this * (displayMetrics?.density ?: 1.0f)) + 0.5).toFloat()
}

fun Double.dpToPx(context: Context?): Double {
    val displayMetrics = context?.resources?.displayMetrics
    return ((this * (displayMetrics?.density ?: 1.0f)) + 0.5)
}
