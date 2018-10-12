package com.habitrpg.android.habitica.extensions

import io.reactivex.Completable
import java.util.concurrent.TimeUnit

/**
 * Created by phillip on 01.02.18.
 */

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun runDelayed(interval: Long, timeUnit: TimeUnit, function: () -> Unit) {
    Completable.complete().delay(interval, timeUnit)
            .subscribe(function)
}