package com.habitrpg.shared.habitica.interactors

actual class PlatformLogger actual constructor() {
    actual val enabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual fun logDebug(tag: String, message: String) {
    }

    actual fun logInfo(tag: String, message: String) {
    }

    actual fun logError(tag: String, message: String) {
    }

    actual fun logError(tag: String, message: String, exception: Throwable) {
    }

}