package com.habitrpg.android.habitica.proxy

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsManagerImpl(context: Context) : AnalyticsManager {

    private val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics

    init {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    override fun logException(t: Throwable) {
        if (t is org.solovyev.android.checkout.BillingException) {
            if (t.response == org.solovyev.android.checkout.ResponseCodes.USER_CANCELED) {
                return
            }
        }
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
