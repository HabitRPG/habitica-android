package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.events.Identify
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R

enum class AnalyticsTarget {
    AMPLITUDE
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
    private const val CONSENT_PREFERENCE_KEY = "analytics_consent_given"

    private lateinit var amplitude: Amplitude
    private var hasConsent: Boolean = false
    private var isInitialized: Boolean = false
    private var knownUserID: String? = null

    @JvmOverloads
    fun sendEvent(
        eventAction: String?,
        category: EventCategory?,
        hitType: HitType?,
        additionalData: Map<String, Any>? = null,
        target: AnalyticsTarget? = null
    ) {
        if (BuildConfig.DEBUG || !hasConsent || !isInitialized) {
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
        amplitude =
            Amplitude(
                Configuration(
                    context.getString(R.string.amplitude_app_id),
                    context,
                    optOut = true,
                )
            )
        isInitialized = true
        applyConsent(
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(CONSENT_PREFERENCE_KEY, false)
        )
    }

    fun identify(sharedPrefs: SharedPreferences) {
        if (!hasConsent || !isInitialized) {
            return
        }
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
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.setUserId(userID)
        }
        FirebaseCrashlytics.getInstance().setUserId(userID)
    }

    private fun clearIdentity() {
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.setUserId(null)
        }
        FirebaseCrashlytics.getInstance().setUserId("")
    }

    fun setUserProperty(
        identifier: String,
        value: Any?
    ) {
        if (!hasConsent || !isInitialized) {
            return
        }
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.identify(mapOf(identifier to value))
        }
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
        executeLambda(AnalyticsTarget.AMPLITUDE) {
            amplitude.configuration.optOut = !isEnabled
        }

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


    private fun executeLambda(analyticsTarget: AnalyticsTarget, action: () -> Unit) {
        when (analyticsTarget) {
            AnalyticsTarget.AMPLITUDE -> if (!::amplitude.isInitialized) return
        }
        action()
    }
}
