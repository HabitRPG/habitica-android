package com.habitrpg.android.habitica.extensions

/**
 * Created by phillip on 01.02.18.
 */

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}