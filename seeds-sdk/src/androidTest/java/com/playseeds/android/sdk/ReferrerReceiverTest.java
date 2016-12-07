package com.playseeds.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;

public class ReferrerReceiverTest extends AndroidTestCase {
    ReferrerReceiver receiver;
    Context context;

    public void setUp() throws Exception {
        receiver = new ReferrerReceiver();
        context = getContext();
    }

    public void testOnReceive() throws Exception {
        Intent intent = new Intent();
        intent.setAction("com.android.vending.INSTALL_REFERRER");
        intent.putExtra("referrer", "testReferrer");

        try {
            receiver.onReceive(context, intent);
        } catch (Exception e) {
        }
    }

    public void testOnReceiveWhenNullIntent() throws Exception {
        try {
            receiver.onReceive(context, null);
        } catch(Exception e) {
            // success
        }
    }

    public void testDeleteReferrer() throws Exception {
        try {
            ReferrerReceiver.deleteReferrer(context);
        } catch(Exception e) {
            //
        }
    }

    public void testGetReferrer() throws Exception {
        String referrer = ReferrerReceiver.getReferrer(context);
        assertNull(referrer);

    }
}
