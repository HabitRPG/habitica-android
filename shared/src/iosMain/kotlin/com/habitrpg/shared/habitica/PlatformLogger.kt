package com.habitrpg.shared.habitica

actual class PlatformLogger {
    actual val enabled: Boolean
        get() = true

    actual fun logDebug(
        tag: String,
        message: String,
    ) {
        println("[🟢] $tag: $message")
    }

    actual fun logInfo(
        tag: String,
        message: String,
    ) {
        println("[🟡] $tag: $message")
    }

    actual fun logWarning(
        tag: String,
        message: String,
    ) {
        println("[🟠] $tag: $message")
    }

    actual fun logError(
        tag: String,
        message: String,
    ) {
        println("[🔴] $tag: $message")
    }

    actual fun logError(
        tag: String,
        message: String,
        exception: Throwable,
    ) {
        println("[🔴] $tag: $message\n${exception.getStackTrace().joinToString("\n")}")
    }
}
