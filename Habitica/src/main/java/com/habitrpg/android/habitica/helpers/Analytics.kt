package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsManager {
    fun initialize(context: Context)

    fun setUserID(userID: String)

    fun clearUserID()

    fun logError(msg: String)

    fun logException(t: Throwable)

    fun setAnalyticsConsent(consents: Boolean?)
}

@Singleton
class FirebaseAnalyticsManager
    @Inject
    constructor() : AnalyticsManager {
        private var hasConsent: Boolean = false
        private var isInitialized: Boolean = false
        private var knownUserID: String? = null

        override fun initialize(context: Context) {
            isInitialized = true
            applyConsent(
                PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getBoolean(CONSENT_PREFERENCE_KEY, false),
            )
        }

        override fun setUserID(userID: String) {
            knownUserID = userID.ifBlank { null }
            if (!hasConsent || !isInitialized) {
                clearIdentity()
                return
            }
            applyIdentity(userID)
        }

        override fun clearUserID() {
            knownUserID = null
            clearIdentity()
        }

        private fun applyIdentity(userID: String) {
            FirebaseCrashlytics.getInstance().setUserId(userID)
        }

        private fun clearIdentity() {
            FirebaseCrashlytics.getInstance().setUserId("")
        }

        override fun logError(msg: String) {
            if (!hasConsent) {
                return
            }
            FirebaseCrashlytics.getInstance().log(msg)
        }

        override fun logException(t: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }

        override fun setAnalyticsConsent(consents: Boolean?) {
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

        companion object {
            private const val CONSENT_PREFERENCE_KEY = "analytics_consent_given"
        }
    }
