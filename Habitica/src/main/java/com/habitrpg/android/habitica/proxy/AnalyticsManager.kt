package com.habitrpg.android.habitica.proxy

import android.os.Bundle

interface AnalyticsManager {
    fun logException(t: Throwable)
    fun setUserIdentifier(identifier: String)
    fun logError(msg: String)
    fun logEvent(eventName: String, data: Bundle)
}
