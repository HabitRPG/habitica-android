package com.habitrpg.android.habitica.proxy

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.helpers.AmplitudeManager

class AnalyticsManagerImpl(context: Context) : AnalyticsManager {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    override fun setUserIdentifier(identifier: String) {
        FirebaseCrashlytics.getInstance().setUserId(identifier)
        AmplitudeManager.amplitude.setUserId(identifier)
    }

    override fun setUserProperty(identifier: String, value: String) {
        firebaseAnalytics.setUserProperty(identifier, value)
    }

    override fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    override fun logEvent(eventName: String, data: Bundle) {
        firebaseAnalytics.logEvent(eventName, data)
    }
}
