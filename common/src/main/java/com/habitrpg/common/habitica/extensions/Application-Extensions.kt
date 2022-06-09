package com.habitrpg.common.habitica.extensions

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.DebugLogger
import com.habitrpg.common.habitica.BuildConfig

fun Application.setupCoil() {
    var builder = ImageLoader.Builder(this)
        .allowHardware(false)
        .componentRegistry {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder(this@setupCoil))
            } else {
                add(GifDecoder())
            }
        }
    if (BuildConfig.DEBUG) {
        builder = builder.logger(DebugLogger())
    }
    Coil.setImageLoader(builder.build())
}