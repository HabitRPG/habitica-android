package com.playseeds.android.sdk;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OpenUDIDAdapter {
    private final static String OPEN_UDID_MANAGER_CLASS_NAME = "org.openudid.OpenUDID_manager";

    public static boolean isOpenUDIDAvailable() {
        boolean openUDIDAvailable = false;
        try {
            Class.forName(OPEN_UDID_MANAGER_CLASS_NAME);
            openUDIDAvailable = true;
        }
        catch (ClassNotFoundException ignored) {}
        return openUDIDAvailable;
    }

    public static boolean isInitialized() {
        boolean initialized = false;
        try {
            final Class<?> cls = Class.forName(OPEN_UDID_MANAGER_CLASS_NAME);
            final Method isInitializedMethod = cls.getMethod("isInitialized", (Class[]) null);
            final Object result = isInitializedMethod.invoke(null, (Object[]) null);
            if (result instanceof Boolean) {
                initialized = (Boolean) result;
            }
        }
        catch (ClassNotFoundException ignored) {}
        catch (NoSuchMethodException ignored) {}
        catch (InvocationTargetException ignored) {}
        catch (IllegalAccessException ignored) {}
        return initialized;
    }

    public static void sync(final Context context) {
        try {
            final Class<?> cls = Class.forName(OPEN_UDID_MANAGER_CLASS_NAME);
            final Method syncMethod = cls.getMethod("sync", Context.class);
            syncMethod.invoke(null, context);
        }
        catch (ClassNotFoundException ignored) {}
        catch (NoSuchMethodException ignored) {}
        catch (InvocationTargetException ignored) {}
        catch (IllegalAccessException ignored) {}
    }

    public static String getOpenUDID() {
        String openUDID = null;
        try {
            final Class<?> cls = Class.forName(OPEN_UDID_MANAGER_CLASS_NAME);
            final Method getOpenUDIDMethod = cls.getMethod("getOpenUDID", (Class[]) null);
            final Object result = getOpenUDIDMethod.invoke(null, (Object[]) null);
            if (result instanceof String) {
                openUDID = (String) result;
            }
        }
        catch (ClassNotFoundException ignored) {}
        catch (NoSuchMethodException ignored) {}
        catch (InvocationTargetException ignored) {}
        catch (IllegalAccessException ignored) {}
        return openUDID;
    }
}
