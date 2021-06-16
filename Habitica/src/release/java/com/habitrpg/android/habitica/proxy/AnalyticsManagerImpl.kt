package com.habitrpg.android.habitica.proxy

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.amplitude.api.Amplitude
import android.os.Bundle

class AnalyticsManagerImpl(context: Context): AnalyticsManager {

    private val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics

    init {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    override fun setUserIdentifier(identifier: String) {
        FirebaseCrashlytics.getInstance().setUserId(identifier)
        Amplitude.getInstance().userId = identifier
    }

    override fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    override fun logEvent(eventName: String, data: Bundle) {
        firebaseAnalytics.logEvent(eventName, data)
    }
}