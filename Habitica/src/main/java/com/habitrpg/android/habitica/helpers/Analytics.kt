package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.bundleOf
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.events.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R

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
    private lateinit var firebase: FirebaseAnalytics
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
        val data =
            mutableMapOf<String, Any?>(
                "eventAction" to eventAction,
                "eventCategory" to category?.key,
                "hitType" to hitType?.key,
                "status" to "displayed"
            )
        if (additionalData != null) {
            data.putAll(additionalData)
        }
        if (eventAction != null) {
            if (this::amplitude.isInitialized) {
                if (target == null || target == AnalyticsTarget.AMPLITUDE) {
                    amplitude.track(eventAction, data)
                }
            }
            if (this::firebase.isInitialized) {
                if (target == null || target == AnalyticsTarget.FIREBASE) {
                    firebase.logEvent(eventAction, bundleOf(*data.toList().toTypedArray()))
                }
            }
        }
    }

    fun sendNavigationEvent(page: String) {
        val additionalData = HashMap<String, Any>()
        additionalData["page"] = page
        sendEvent("navigated $page", EventCategory.NAVIGATION, HitType.PAGEVIEW, additionalData)
    }

    fun initialize(context: Context) {
        amplitude =
            Amplitude(
                Configuration(
                    context.getString(R.string.amplitude_app_id),
                    context,
                    optOut = true,
                )
            )
        firebase = FirebaseAnalytics.getInstance(context)
    }

    fun identify(sharedPrefs: SharedPreferences) {
        val identify =
            Identify()
                .setOnce("androidStore", BuildConfig.STORE)
        sharedPrefs.getString("launch_screen", "")?.let {
            identify.set("launch_screen", it)
        }
        if (this::amplitude.isInitialized) {
            amplitude.identify(identify)
        }
    }

    fun setUserID(userID: String) {
        if (this::amplitude.isInitialized) {
            amplitude.setUserId(userID)
        }
        FirebaseCrashlytics.getInstance().setUserId(userID)
        if (this::firebase.isInitialized) {
            firebase.setUserId(userID)
        }
    }

    fun setUserProperty(
        identifier: String,
        value: Any?
    ) {
        if (this::amplitude.isInitialized) {
            amplitude.identify(mapOf(identifier to value))
        }
        if (this::firebase.isInitialized) {
            firebase.setUserProperty(identifier, value?.toString())
        }
    }

    fun logError(msg: String) {
        FirebaseCrashlytics.getInstance().log(msg)
    }

    fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    fun setAnalyticsConsent(consents: Boolean?) {
        if (consents == true) {
            firebase.setAnalyticsCollectionEnabled(true)
            amplitude.configuration.optOut = false
        } else {
            firebase.setAnalyticsCollectionEnabled(false)
            amplitude.configuration.optOut = true
        }
    }
}
