package com.habitrpg.shared.habitica

import android.util.Log

actual class PlatformLogger actual constructor() {
    actual val enabled: Boolean
        get() = true

    actual fun logDebug(
        tag: String,
        message: String,
    ) {
        Log.d(tag, message)
    }

    actual fun logInfo(
        tag: String,
        message: String,
    ) {
        Log.i(tag, message)
    }

    actual fun logWarning(
        tag: String,
        message: String,
    ) {
        Log.w(tag, message)
    }

    actual fun logError(
        tag: String,
        message: String,
    ) {
        Log.e(tag, message)
    }

    actual fun logError(
        tag: String,
        message: String,
        exception: Throwable,
    ) {
        Log.e(tag, message, exception)
    }
}
