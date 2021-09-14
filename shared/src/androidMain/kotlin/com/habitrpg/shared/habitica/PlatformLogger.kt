package com.habitrpg.shared.habitica

import android.util.Log
import space.thelen.shared.cluetective.BuildConfig

actual class PlatformLogger actual constructor() {
    actual val enabled: Boolean
        get() = BuildConfig.DEBUG

    actual fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
    }

    actual fun logError(tag: String, message: String) {
        Log.e(tag, message)
    }

    actual fun logError(tag: String, message: String, exception: Throwable) {
        Log.e(tag, message, exception)
    }
}
