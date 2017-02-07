package com.habitrpg.android.habitica.helpers;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by viirus on 23-Sep-16.
 */

public class AmplitudeManager {

    public static String EVENT_CATEGORY_BEHAVIOUR = "behaviour";
    public static String EVENT_CATEGORY_NAVIGATION = "navigation";

    public static String EVENT_HITTYPE_EVENT = "event";
    public static String EVENT_HITTYPE_PAGEVIEW = "pageview";

    public static void sendEvent(String eventAction, String eventCategory, String hitType) {
        sendEvent(eventAction, eventCategory, hitType, null);
    }

    public static void sendEvent(String eventAction, String eventCategory, String hitType, Map<String, Object> additionalData) {
        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("eventAction", eventAction);
            eventProperties.put("eventCategory", eventCategory);
            eventProperties.put("hitType", hitType);
            eventProperties.put("status", "displayed");
            if (additionalData != null) {
                for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
                    eventProperties.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException exception) {
        }
        Amplitude.getInstance().logEvent(eventAction, eventProperties);
    }

}
