package com.habitrpg.common.habitica.extensions

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.habitrpg.common.habitica.BuildConfig

fun Application.setupCoil() {
    var builder =
        ImageLoader.Builder(this)
            .allowHardware(false)
            .crossfade(false)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
    if (BuildConfig.DEBUG) {
        builder = builder.logger(DebugLogger())
    }
    SingletonImageLoader.setSafe { builder.build() }
}
