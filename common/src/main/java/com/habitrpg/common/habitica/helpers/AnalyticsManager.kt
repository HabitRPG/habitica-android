package com.habitrpg.common.habitica.helpers

import android.os.Bundle

interface AnalyticsManager {
    fun logException(t: Throwable)
    fun setUserIdentifier(identifier: String)
    fun setUserProperty(identifier: String, value: String)
    fun logError(msg: String)
    fun logEvent(eventName: String, data: Bundle)
}
