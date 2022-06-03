package com.habitrpg.common.habitica.extensions

import android.content.Context

fun Int.dpToPx(context: Context?): Int {
    val displayMetrics = context?.resources?.displayMetrics
    return ((this * (displayMetrics?.density ?: 1.0f)) + 0.5).toInt()
}
