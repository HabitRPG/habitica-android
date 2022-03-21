package com.habitrpg.android.habitica.extensions

import android.app.PendingIntent
import android.os.Build

fun withImmutableFlag(flags: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        flags + PendingIntent.FLAG_IMMUTABLE
    } else {
        flags
    }
}

fun withMutableFlag(flags: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        flags + PendingIntent.FLAG_MUTABLE
    } else {
        flags
    }
}
