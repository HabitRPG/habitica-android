package com.habitrpg.android.habitica.proxy

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManagerImpl: AnalyticsManager {

    private val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics

    init(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun logException(t: Throwabl?) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    override fun setUserIdentifier(identifier: String) {
        FirebaseCrashlytics.getInstance().setUserId(identifier)
        Amplitude.getInstance().userId = identifier
    }

    override fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logEvent(eventName: String, data: Bundle) {
        firebaseAnalytics.logEvent(eventName, data)
    }
}