package com.habitrpg.shared.habitica

expect class PlatformLogger() {
    val enabled: Boolean

    fun logDebug(
        tag: String,
        message: String
    )

    fun logInfo(
        tag: String,
        message: String
    )

    fun logWarning(
        tag: String,
        message: String
    )

    fun logError(
        tag: String,
        message: String
    )

    fun logError(
        tag: String,
        message: String,
        exception: Throwable
    )
}

enum class LogLevel {
    ERROR,
    INFO,
    WARNING,
    DEBUG
}

class HLogger {
    companion object {
        private val platformLogger = PlatformLogger()

        val enabled
            get() = platformLogger.enabled

        fun log(
            level: LogLevel,
            tag: String,
            message: String
        ) {
            if (!enabled) return
            when (level) {
                LogLevel.ERROR -> platformLogger.logError(tag, message)
                LogLevel.INFO -> platformLogger.logInfo(tag, message)
                LogLevel.WARNING -> platformLogger.logWarning(tag, message)
                LogLevel.DEBUG -> platformLogger.logDebug(tag, message)
            }
        }

        fun logException(
            tag: String,
            message: String,
            exception: Throwable? = null
        ) {
            if (!enabled) return
            exception?.let {
                platformLogger.logError(tag, message, exception)
            } ?: run {
                platformLogger.logError(tag, message)
            }
        }
    }
}
