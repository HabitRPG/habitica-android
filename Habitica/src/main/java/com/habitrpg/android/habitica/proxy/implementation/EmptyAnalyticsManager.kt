package com.habitrpg.android.habitica.proxy.implementation

import android.os.Bundle
import com.habitrpg.android.habitica.proxy.AnalyticsManager

class EmptyAnalyticsManager : AnalyticsManager {

    override fun logException(e: Throwable) {
        // pass
    }

    override fun setUserIdentifier(identifier: String) {
        // pass
    }

    override fun logError(msg: String) {
        // pass
    }

    override fun logEvent(eventName: String, data: Bundle) {
        // pass
    }
}
