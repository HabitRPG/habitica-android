@file:OptIn(ExperimentalJsExport::class)

package com.habitrpg.shared.habitica

@JsExport
actual class PlatformLogger actual constructor() {
    actual val enabled: Boolean
        get() = true

    actual fun logDebug(
        tag: String,
        message: String,
    ) {
        console.log("[🥦] $tag: $message")
    }

    actual fun logInfo(
        tag: String,
        message: String,
    ) {
        console.log("[🍋] $tag: $message")
    }

    actual fun logWarning(
        tag: String,
        message: String,
    ) {
        console.log("[🍊] $tag: $message")
    }

    actual fun logError(
        tag: String,
        message: String,
    ) {
        console.log("[🍎] $tag: $message")
    }

    @JsName("logErrorException")
    actual fun logError(
        tag: String,
        message: String,
        exception: Throwable,
    ) {
        console.log("[🍎] $tag: $message\n$exception")
    }
}
