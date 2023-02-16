package com.habitrpg.android.habitica.proxy.implementation

import android.os.Bundle
import com.habitrpg.common.habitica.helpers.AnalyticsManager

class EmptyAnalyticsManager : AnalyticsManager {

    override fun logException(t: Throwable) {
        // pass
    }

    override fun setUserIdentifier(identifier: String) {
        // pass
    }

    override fun setUserProperty(identifier: String, value: String) {
        // pass
    }

    override fun logError(msg: String) {
        // pass
    }

    override fun logEvent(eventName: String, data: Bundle) {
        // pass
    }
}
