package com.playseeds.android.sdk;

import android.content.Context;
import android.test.AndroidTestCase;

public class AdvertisingIdAdapterTest extends AndroidTestCase {
    Context context;

    public void setUp() throws Exception {
        context = getContext();
    }

    public void testIsAdvertisingIdAvailable() throws Exception {
        assertNotNull(AdvertisingIdAdapter.isAdvertisingIdAvailable());
    }

    public void testSetAdvertisingId() throws Exception {
        try {
            AdvertisingIdAdapter.setAdvertisingId(context, new CountlyStore(context), new DeviceId(DeviceId.Type.ADVERTISING_ID));
        } catch(Exception e) {

        }
    }

    public void testSetAdvertisingIdWithNullContext() throws Exception {
        try {
            AdvertisingIdAdapter.setAdvertisingId(null, new CountlyStore(context), new DeviceId(DeviceId.Type.ADVERTISING_ID));
        } catch (Exception e) {

        }
    }
}
