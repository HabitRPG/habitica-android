package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.bundleOf
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.events.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.BuildConfig

enum class AnalyticsTarget {
    AMPLITUDE,
    FIREBASE
}

enum class EventCategory(val key: String) {
    BEHAVIOUR("behaviour"),
    NAVIGATION("navigation")
}

enum class HitType(val key: String) {
    EVENT("event"),
    PAGEVIEW("pageview"),
    CREATE_WIDGET("create"),
    REMOVE_WIDGET("remove"),
    UPDATE_WIDGET("update")
}

object Analytics {
    private lateinit var firebase : FirebaseAnalytics
    private lateinit var amplitude: Amplitude

    @JvmOverloads
    fun sendEvent(
        eventAction: String?,
        category: EventCategory?,
        hitType: HitType?,
        additionalData: Map<String, Any>? = null,
        target: AnalyticsTarget? = null
    ) {
        if (BuildConfig.DEBUG) {
            return
        }
        val data = mutableMapOf<String, Any?>(
            "eventAction" to eventAction,
            "eventCategory" to category?.key,
            "hitType" to hitType?.key,
            "status" to "displayed"
        )
        if (additionalData != null) {
            data.putAll(additionalData)
        }
        if (eventAction != null) {
            if (target == null || target == AnalyticsTarget.AMPLITUDE) {
                amplitude.track(eventAction, data)
            }
            if (target == null || target == AnalyticsTarget.FIREBASE) {
                firebase.logEvent(eventAction, bundleOf(*data.toList().toTypedArray()))
            }
        }
    }

    fun sendNavigationEvent(page: String) {
        val additionalData = HashMap<String, Any>()
        additionalData["page"] = page
        sendEvent("navigated", EventCategory.NAVIGATION, HitType.PAGEVIEW, additionalData)
    }

    fun initialize(context: Context) {
        amplitude = Amplitude(
            Configuration(
                context.getString(R.string.amplitude_app_id),
                context
            )
        )
        firebase = FirebaseAnalytics.getInstance(context)
    }

    fun identify(sharedPrefs: SharedPreferences) {
        val identify = Identify()
            .setOnce("androidStore", BuildConfig.STORE)
        sharedPrefs.getString("launch_screen", "")?.let {
            identify.set("launch_screen", it)
        }
        amplitude.identify(identify)
    }

    fun setUserID(userID: String) {
        amplitude.setUserId(userID)
        FirebaseCrashlytics.getInstance().setUserId(userID)
        firebase.setUserId(userID)
    }

    fun setUserProperty(identifier: String, value: String) {
        firebase.setUserProperty(identifier, value)
    }

    fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }
}
