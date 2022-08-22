package com.habitrpg.shared.habitica

actual class PlatformLogger {
    actual val enabled: Boolean
        get() = true

    actual fun logDebug(tag: String, message: String) {
        println("[DEBUG] $tag: $message")
    }

    actual fun logInfo(tag: String, message: String) {
        println("[INFO] $tag: $message")
    }

    actual fun logError(tag: String, message: String) {
        println("[ERROR] $tag: $message")
    }

    actual fun logError(tag: String, message: String, exception: Throwable) {
        println("[ERROR] $tag: $message\n${exception.getStackTrace().joinToString("\n")}")
    }
}