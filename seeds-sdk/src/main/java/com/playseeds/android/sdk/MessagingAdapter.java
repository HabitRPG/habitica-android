package com.playseeds.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class MessagingAdapter {
    private static final String TAG = "MessagingAdapter";
    private final static String MESSAGING_CLASS_NAME = "ly.count.android.sdk.messaging.CountlyMessaging";

    public static boolean isMessagingAvailable() {
        boolean messagingAvailable = false;
        try {
            Class.forName(MESSAGING_CLASS_NAME);
            messagingAvailable = true;
        }
        catch (ClassNotFoundException ignored) {}
        return messagingAvailable;
    }

    public static boolean init(Activity activity, Class<? extends Activity> activityClass, String sender, String[] buttonNames) {
        try {
            final Class<?> cls = Class.forName(MESSAGING_CLASS_NAME);
            final Method method = cls.getMethod("init", Activity.class, Class.class, String.class, String[].class);
            method.invoke(null, activity, activityClass, sender, buttonNames);
            return true;
        }
        catch (Throwable logged) {
            Log.e(TAG, "Couldn't init Seeds Messaging", logged);
            return false;
        }
    }

    public static boolean storeConfiguration(Context context, String serverURL, String appKey, String deviceID, DeviceId.Type idMode) {
        try {
            final Class<?> cls = Class.forName(MESSAGING_CLASS_NAME);
            final Method method = cls.getMethod("storeConfiguration", Context.class, String.class, String.class, String.class, DeviceId.Type.class);
            method.invoke(null, context, serverURL, appKey, deviceID, idMode);
            return true;
        }
        catch (Throwable logged) {
            Log.e(TAG, "Couldn't store configuration in Seeds Messaging", logged);
            return false;
        }
    }
}
