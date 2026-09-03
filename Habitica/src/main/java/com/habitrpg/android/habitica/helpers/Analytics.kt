package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

object Analytics {
    private const val CONSENT_PREFERENCE_KEY = "analytics_consent_given"

    private var hasConsent: Boolean = false
    private var isInitialized: Boolean = false
    private var knownUserID: String? = null

    fun initialize(context: Context) {
        isInitialized = true
        applyConsent(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(CONSENT_PREFERENCE_KEY, false)
        )
    }

    fun setUserID(userID: String) {
        knownUserID = userID.ifBlank { null }
        if (!hasConsent || !isInitialized) {
            clearIdentity()
            return
        }
        applyIdentity(userID)
    }

    fun clearUserID() {
        knownUserID = null
        clearIdentity()
    }

    private fun applyIdentity(userID: String) {
        FirebaseCrashlytics.getInstance().setUserId(userID)
    }

    private fun clearIdentity() {
        FirebaseCrashlytics.getInstance().setUserId("")
    }

    fun logError(msg: String) {
        if (!hasConsent) {
            return
        }
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    fun setAnalyticsConsent(consents: Boolean?) {
        applyConsent(consents == true)
    }

    private fun applyConsent(isEnabled: Boolean) {
        val wasEnabled = hasConsent
        hasConsent = isEnabled

        if (!isInitialized) {
            return
        }

        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = isEnabled

        val userID = knownUserID
        if (isEnabled && userID != null) {
            applyIdentity(userID)
        } else {
            clearIdentity()
        }

        if (wasEnabled && !isEnabled) {
            FirebaseCrashlytics.getInstance().deleteUnsentReports()
        }
    }
}
