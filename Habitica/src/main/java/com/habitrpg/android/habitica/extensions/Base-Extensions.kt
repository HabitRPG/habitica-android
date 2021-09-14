package com.habitrpg.android.habitica.extensions

import io.reactivex.rxjava3.core.Completable
import java.util.concurrent.TimeUnit

/**
 * Created by phillip on 01.02.18.
 */

fun runDelayed(interval: Long, timeUnit: TimeUnit, function: () -> Unit) {
    Completable.complete().delay(interval, timeUnit)
        .subscribe(function)
}
