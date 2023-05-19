package com.habitrpg.android.habitica.helpers

import android.content.Context
import android.content.SharedPreferences
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.events.Identify
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R

object AmplitudeManager {
    var EVENT_CATEGORY_BEHAVIOUR = "behaviour"
    var EVENT_CATEGORY_NAVIGATION = "navigation"
    var EVENT_HITTYPE_EVENT = "event"
    var EVENT_HITTYPE_PAGEVIEW = "pageview"
    var EVENT_HITTYPE_CREATE_WIDGET = "create"
    var EVENT_HITTYPE_REMOVE_WIDGET = "remove"
    var EVENT_HITTYPE_UPDATE_WIDGET = "update"

    lateinit var amplitude: Amplitude

    @JvmOverloads
    fun sendEvent(
        eventAction: String?,
        eventCategory: String?,
        hitType: String?,
        additionalData: Map<String, Any>? = null
    ) {
        if (BuildConfig.DEBUG) {
            return
        }
        val data = mutableMapOf<String, Any?>(
            "eventAction" to eventAction,
            "eventCategory" to eventCategory,
            "hitType" to hitType,
            "status" to "displayed"
        )
        if (additionalData != null) {
            for ((key, value) in additionalData) {
                data.put(key, value)
            }
        }
        if (eventAction != null) {
            amplitude.track(eventAction, data)
        }
    }

    fun sendNavigationEvent(page: String) {
        val additionalData = HashMap<String, Any>()
        additionalData["page"] = page
        sendEvent("navigated", EVENT_CATEGORY_NAVIGATION, EVENT_HITTYPE_PAGEVIEW, additionalData)
    }

    fun initialize(context: Context) {
        amplitude = Amplitude(
            Configuration(
                context.getString(R.string.amplitude_app_id),
                context
            )
        )
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
    }
}
