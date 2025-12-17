package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.bundleOf
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.events.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
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
            executeLambda(AnalyticsTarget.AMPLITUDE) {
                if (target == null || target == AnalyticsTarget.AMPLITUDE) {
                    amplitude.track(eventAction, data)
                }
            }
            executeLambda(AnalyticsTarget.FIREBASE) {
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
        val amplitudeAppId = context.getString(R.string.amplitude_app_id)
        if (amplitudeAppId.isNullOrBlank()) {
            // No amplitude configuration provided; skip amplitude setup for this build.
        } else {
            amplitude =
                Amplitude(
                    Configuration(
                        amplitudeAppId,
                        context,
                        optOut = true,
                    )
                )
        }
        firebase = FirebaseAnalytics.getInstance(context)
    }

    fun identify(sharedPrefs: SharedPreferences) {
        val identify =
            Identify()
                .setOnce("androidStore", BuildConfig.STORE)
        sharedPrefs.getString("launch_screen", "")?.let {
            identify.set("launch_screen", it)
        }
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.identify(identify)
        }
    }

    fun setUserID(userID: String) {
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.setUserId(userID)
        }
        FirebaseCrashlytics.getInstance().setUserId(userID)
        executeLambda(AnalyticsTarget.FIREBASE) {
            firebase.setUserId(userID)
        }
    }

    fun setUserProperty(
        identifier: String,
        value: Any?
    ) {
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.identify(mapOf(identifier to value))
        }
        executeLambda(AnalyticsTarget.FIREBASE) {
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
        val isEnabled = consents == true
        executeLambda(AnalyticsTarget.FIREBASE) {
            firebase.setAnalyticsCollectionEnabled(isEnabled)
        }
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = isEnabled
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.configuration.optOut = !isEnabled
        }
    }


    private fun executeLambda(analyticsTarget: AnalyticsTarget, action: () -> Unit) {
        when (analyticsTarget) {
            AnalyticsTarget.AMPLITUDE -> if (!::amplitude.isInitialized) return
            AnalyticsTarget.FIREBASE -> if (!::firebase.isInitialized) return
        }
        action()
    }
}
