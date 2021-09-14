package com.habitrpg.android.habitica.helpers

import com.amplitude.api.Amplitude
import com.habitrpg.android.habitica.BuildConfig
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

object AmplitudeManager {
    var EVENT_CATEGORY_BEHAVIOUR = "behaviour"
    var EVENT_CATEGORY_NAVIGATION = "navigation"
    var EVENT_HITTYPE_EVENT = "event"
    var EVENT_HITTYPE_PAGEVIEW = "pageview"
    var EVENT_HITTYPE_CREATE_WIDGET = "create"
    var EVENT_HITTYPE_REMOVE_WIDGET = "remove"
    var EVENT_HITTYPE_UPDATE_WIDGET = "update"

    @JvmOverloads
    fun sendEvent(eventAction: String?, eventCategory: String?, hitType: String?, additionalData: Map<String, Any>? = null) {
        if (BuildConfig.DEBUG) {
            return
        }
        val eventProperties = JSONObject()
        try {
            eventProperties.put("eventAction", eventAction)
            eventProperties.put("eventCategory", eventCategory)
            eventProperties.put("hitType", hitType)
            eventProperties.put("status", "displayed")
            if (additionalData != null) {
                for ((key, value) in additionalData) {
                    eventProperties.put(key, value)
                }
            }
        } catch (exception: JSONException) {
        }
        Amplitude.getInstance().logEvent(eventAction, eventProperties)
    }

    fun sendNavigationEvent(page: String) {
        val additionalData = HashMap<String, Any>()
        additionalData["page"] = page
        sendEvent("navigated", EVENT_CATEGORY_NAVIGATION, EVENT_HITTYPE_PAGEVIEW, additionalData)
    }
}
