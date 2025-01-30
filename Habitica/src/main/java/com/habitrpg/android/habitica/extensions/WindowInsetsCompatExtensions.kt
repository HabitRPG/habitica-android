package com.habitrpg.android.habitica.extensions

import android.os.Build
import androidx.core.view.WindowInsetsCompat

fun consumeWindowInsetsAbove30(insets: WindowInsetsCompat): WindowInsetsCompat {
    if (Build.VERSION.SDK_INT >= 30) return WindowInsetsCompat.CONSUMED
    return insets
}
