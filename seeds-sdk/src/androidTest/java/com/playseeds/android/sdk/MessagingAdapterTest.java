package com.playseeds.android.sdk;

import android.content.Context;
import android.test.AndroidTestCase;

public class MessagingAdapterTest extends AndroidTestCase {

    public void testIsMessagingAvailable() throws Exception {
        boolean isAvailable = MessagingAdapter.isMessagingAvailable();
        assertNotNull(isAvailable);
    }

    public void testInit() throws Exception {
        boolean isInit = MessagingAdapter.init(null, null, null, null);
        assertFalse(isInit);
    }

    public void testStoreConfiguration() throws Exception {
        Context context = getContext();
        boolean config = MessagingAdapter.storeConfiguration(context,
                "http://www.devdash.playseeds.com", "appkey", "id", DeviceId.Type.ADVERTISING_ID);
        assertNotNull(config);
    }
}
