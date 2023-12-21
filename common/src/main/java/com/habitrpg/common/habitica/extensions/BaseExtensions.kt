package com.habitrpg.common.habitica.extensions

import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Created by phillip on 01.02.18.
 */

fun runDelayed(interval: Long, timeUnit: DurationUnit, function: () -> Unit) {
    MainScope().launchCatching {
        delay(interval.toDuration(timeUnit))
        function()
    }
}
