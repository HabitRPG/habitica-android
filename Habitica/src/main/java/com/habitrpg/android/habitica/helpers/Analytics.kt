package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.BuildConfig

enum class EventCategory(
    val key: String,
) {
    BEHAVIOUR("behaviour"),
    NAVIGATION("navigation"),
}

enum class HitType(
    val key: String,
) {
    EVENT("event"),
    PAGEVIEW("pageview"),
    CREATE_WIDGET("create"),
    REMOVE_WIDGET("remove"),
    UPDATE_WIDGET("update"),
}

object Analytics {
    private var hasConsent: Boolean = false
    private var isInitialized: Boolean = false

    @JvmOverloads
    fun sendEvent(
        eventAction: String?,
        category: EventCategory?,
        hitType: HitType?,
        additionalData: Map<String, Any>? = null,
    ) {
        if (BuildConfig.DEBUG || !hasConsent || !isInitialized) {
            return
        }
        val data =
            mutableMapOf<String, Any?>(
                "eventAction" to eventAction,
                "eventCategory" to category?.key,
                "hitType" to hitType?.key,
                "status" to "displayed",
            )
        if (additionalData != null) {
            data.putAll(additionalData)
        }
    }

    fun sendNavigationEvent(page: String) {
        if (!hasConsent || !isInitialized) {
            return
        }
        val additionalData = HashMap<String, Any>()
        additionalData["page"] = page
        sendEvent("navigated $page", EventCategory.NAVIGATION, HitType.PAGEVIEW, additionalData)
    }

    fun initialize(context: Context) {
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = false
        isInitialized = true
    }

    fun identify(sharedPrefs: SharedPreferences) {
        if (!hasConsent || !isInitialized) {
            return
        }
    }

    fun setUserID(userID: String) {
        if (!hasConsent || !isInitialized) {
            FirebaseCrashlytics.getInstance().setUserId(userID)
            return
        }
        FirebaseCrashlytics.getInstance().setUserId(userID)
    }

    fun clearUserID() {
        FirebaseCrashlytics.getInstance().setUserId("")
    }

    fun setUserProperty(
        identifier: String,
        value: Any?,
    ) {
        if (!hasConsent || !isInitialized) {
            return
        }
    }

    fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    fun setAnalyticsConsent(consents: Boolean?) {
        val isEnabled = consents == true
        hasConsent = isEnabled

        if (!isInitialized) {
            return
        }

        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = isEnabled
    }
}
