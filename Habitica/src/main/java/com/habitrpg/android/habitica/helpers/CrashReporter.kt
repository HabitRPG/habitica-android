package com.habitrpg.android.habitica.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import javax.inject.Inject

interface CrashReporter {
    fun setCustomKey(
        key: String,
        value: String,
    )

    fun logException(throwable: Throwable)

    fun setUserID(userID: String)
    fun logError(msg: String)
    fun setAnalyticsConsent(consents: Boolean?)
    fun initialize(context: Context)
    fun clearUserID()
}

class FirebaseCrashReporter
    @Inject
    constructor() : CrashReporter {

    private var hasConsent: Boolean = false
    private var isInitialized: Boolean = false
    private var knownUserID: String? = null

        override fun setCustomKey(
            key: String,
            value: String,
        ) {
            Firebase.crashlytics.setCustomKey(key, value)
        }

    override fun setUserID(userID: String) {
        knownUserID = userID.ifBlank { null }
        if (!hasConsent || !isInitialized) {
            clearUserID()
            return
        }
        FirebaseCrashlytics.getInstance().setUserId(userID)
    }

    override fun clearUserID() {
        knownUserID = null
        FirebaseCrashlytics.getInstance().setUserId("")
    }

    override fun initialize(context: Context) {
        isInitialized = true
        applyConsent(
            PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(CONSENT_PREFERENCE_KEY, false),
        )
    }

    override fun logError(msg: String) {
        if (!hasConsent) {
            return
        }
        FirebaseCrashlytics.getInstance().log(msg)
    }

    override fun logException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
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

        val userID = knownUserID
        if (isEnabled && userID != null) {
            setUserID(userID)
        } else {
            clearUserID()
        }

        if (wasEnabled && !isEnabled) {
            FirebaseCrashlytics.getInstance().deleteUnsentReports()
        }
    }

    companion object {
        private const val CONSENT_PREFERENCE_KEY = "analytics_consent_given"
    }
}
