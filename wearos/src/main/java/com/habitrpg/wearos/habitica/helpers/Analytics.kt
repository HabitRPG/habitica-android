package com.habitrpg.wearos.habitica.helpers

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.habitrpg.android.habitica.BuildConfig

object Analytics {
    private var hasConsent: Boolean = false
    private var knownUserID: String? = null
    private var appliedUserID: String? = null
    private var hasAppliedIdentity: Boolean = false

    fun initialize() {
        if (BuildConfig.DEBUG) return
        Firebase.crashlytics.setCustomKey("is_wear", true)
        applyIdentity()
    }

    fun setUserID(userID: String?) {
        knownUserID = userID?.ifBlank { null }
        applyIdentity()
    }

    fun setAnalyticsConsent(consents: Boolean?) {
        hasConsent = consents == true
        applyIdentity()
    }

    private fun applyIdentity() {
        if (BuildConfig.DEBUG) return
        val userID = if (hasConsent) knownUserID else null
        if (hasAppliedIdentity && userID == appliedUserID) return
        hasAppliedIdentity = true
        appliedUserID = userID
        Firebase.crashlytics.setUserId(userID ?: "")
    }
}
