package com.playseeds.android.sdk;

import android.content.Context;
import android.util.Log;


public class DeviceId {
    private String id;
    private Type type;
    private static final String TAG = "DeviceId";
    private static final String PREFERENCE_KEY_ID_TYPE = "ly.count.android.api.DeviceId.type";

    /**
     * Enum used throughout Seeds which controls what kind of ID Seeds should use.
     */
    public enum Type {
        DEVELOPER_SUPPLIED,
        OPEN_UDID,
        ADVERTISING_ID,
    }

    /**
     * Initialize DeviceId with Type of OPEN_UDID or ADVERTISING_ID
     * @param type type of ID generation strategy
     */
    public DeviceId(Type type) {
        if (type == null) {
            throw new IllegalStateException("Please specify DeviceId.Type, that is which type of device ID generation you want to use");
        } else if (type == Type.DEVELOPER_SUPPLIED) {
            throw new IllegalStateException("Please use another DeviceId constructor for device IDs supplied by developer");
        }
        this.type = type;
    }

    /**
     * Initialize DeviceId with Developer-supplied id string
     * @param developerSuppliedId Device ID string supplied by developer
     */
    public DeviceId(String developerSuppliedId) {
        if (developerSuppliedId == null || "".equals(developerSuppliedId)) {
            throw new IllegalStateException("Please make sure that device ID is not null or empty");
        }
        this.type = Type.DEVELOPER_SUPPLIED;
        this.id = developerSuppliedId;
    }

    /**
     * Initialize device ID generation, that is start up required services and send requests.
     * Device ID is expected to be available after some time.
     * In some cases, Seeds can override ID generation strategy to other one, for example when
     * Google Play Services are not available and user chose Advertising ID strategy, it will fall
     * back to OpenUDID
     * @param context Context to use
     * @param store CountlyStore to store configuration in
     * @param raiseExceptions whether to raise exceptions in case of illegal state or not
     */
    public void init(Context context, CountlyStore store, boolean raiseExceptions) {
        Type overriddenType = retrieveOverriddenType(store);

        // Some time ago some ID generation strategy was not available and SDK fell back to
        // some other strategy. We still have to use that strategy.
        if (overriddenType != null && overriddenType != type) {
            if (Seeds.sharedInstance().isLoggingEnabled()) {
                Log.i(TAG, "Overridden device ID generation strategy detected: " + overriddenType + ", using it instead of " + this.type);
            }
            type = overriddenType;
        }

        switch (type) {
            case DEVELOPER_SUPPLIED:
                // no initialization for developer id
                break;
            case OPEN_UDID:
                if (OpenUDIDAdapter.isOpenUDIDAvailable()) {
                    if (Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.i(TAG, "Using OpenUDID");
                    }
                    if (!OpenUDIDAdapter.isInitialized()) {
                        OpenUDIDAdapter.sync(context);
                    }
                } else {
                    if (raiseExceptions)
                        throw new IllegalStateException("OpenUDID is not available, please make sure that you have it in your classpath");
                }
                break;
            case ADVERTISING_ID:
                if (AdvertisingIdAdapter.isAdvertisingIdAvailable()) {
                    if (Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.i(TAG, "Using Advertising ID");
                    }
                    AdvertisingIdAdapter.setAdvertisingId(context, store, this);
                } else if (OpenUDIDAdapter.isOpenUDIDAvailable()) {
                    // Fall back to OpenUDID on devices without google play services set up
                    if (Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.i(TAG, "Advertising ID is not available, falling back to OpenUDID");
                    }
                    if (!OpenUDIDAdapter.isInitialized()) {
                        OpenUDIDAdapter.sync(context);
                    }
                } else {
                    // just do nothing, without Advertising ID and OpenUDID this user is lost for Seeds
                    if (Seeds.sharedInstance().isLoggingEnabled()) {
                        Log.w(TAG, "Advertising ID is not available, neither OpenUDID is");
                    }
                    if (raiseExceptions)
                        throw new IllegalStateException("OpenUDID is not available, please make sure that you have it in your classpath");
                }
                break;
        }
    }

    private void storeOverriddenType(CountlyStore store, Type type) {
        // Using strings is safer when it comes to extending Enum values list
        store.setPreference(PREFERENCE_KEY_ID_TYPE, type == null ? null : type.toString());
    }

    private Type retrieveOverriddenType(CountlyStore store) {
        Type oldType;

        // Using strings is safer when it comes to extending Enum values list
        String oldTypeString = store.getPreference(PREFERENCE_KEY_ID_TYPE);
        if (oldTypeString == null) {
            oldType = null;
        } else if (oldTypeString.equals(Type.DEVELOPER_SUPPLIED.toString())) {
            oldType = Type.DEVELOPER_SUPPLIED;
        } else if (oldTypeString.equals(Type.OPEN_UDID.toString())) {
            oldType = Type.OPEN_UDID;
        } else if (oldTypeString.equals(Type.ADVERTISING_ID.toString())) {
            oldType = Type.ADVERTISING_ID;
        } else {
            oldType = null;
        }
        return oldType;
    }

    protected void switchToIdType(Type type, Context context, CountlyStore store) {
        if (Seeds.sharedInstance().isLoggingEnabled()) {
            Log.w(TAG, "Switching to device ID generation strategy " + type + " from " + this.type);
        }
        this.type = type;
        storeOverriddenType(store, type);
        init(context, store, false);
    }

    public String getId() {
        if (id == null && type == Type.OPEN_UDID) {
            id = OpenUDIDAdapter.getOpenUDID();
        }
        return id;
    }

    protected void setId(Type type, String id) {
        if (Seeds.sharedInstance().isLoggingEnabled()) {
            Log.w(TAG, "Device ID is " + id + " (type " + type + ")");
        }
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    /**
     * Helper method for null safe comparison of current device ID and the one supplied to Seeds.init
     * @return true if supplied device ID equal to the one registered before
     */
    static boolean deviceIDEqualsNullSafe(final String id, Type type, final DeviceId deviceId) {
        if (type == null || type == Type.DEVELOPER_SUPPLIED) {
            final String deviceIdId = (deviceId == null) ? null : deviceId.getId();
            return (deviceIdId == null && id == null) || (deviceIdId != null && deviceIdId.equals(id));
        } else {
            return true;
        }
    }
}
