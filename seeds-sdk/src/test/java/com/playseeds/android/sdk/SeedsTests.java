package com.playseeds.android.sdk;

import android.app.Activity;
import android.content.Context;

import com.playseeds.android.sdk.inappmessaging.InAppMessage;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(SeedsTestsRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SeedsTests {
    public static final String SERVER = "http://devdash.playseeds.com";
    public static final String NO_ADS_APP_KEY = "ef2444ec9f590d24db5054fad8385991138a394b";
    public static final String UNLIMITED_ADS_APP_KEY = "c30f02a55541cbe362449d29d83d777c125c8dd6";
    Context context;
    Seeds seeds;

    @Before
    public void setUp() {
        context = ShadowApplication.getInstance().getApplicationContext();
        seeds = Seeds.sharedInstance();
    }

    @After
    public void tearDown() {
        seeds.clear();
    }

    @Test
    public void testSeedsCustomId() {
        seeds.init(context, null, null, SERVER, UNLIMITED_ADS_APP_KEY, "some fake");
    }

    @Test
    public void testSeedsCustomIdExplicit() {
        seeds.init(context, null, null, SERVER, UNLIMITED_ADS_APP_KEY, "some fake", DeviceId.Type.ADVERTISING_ID);
    }

    @Test
    public void testSeedsUDIDUsage() {
        seeds.init(context, null, null, SERVER, UNLIMITED_ADS_APP_KEY, null, DeviceId.Type.OPEN_UDID);

    }

    @Test
    public void testSeedsAdIdUsage() {
        seeds.init(context, null, null, SERVER, UNLIMITED_ADS_APP_KEY, null, DeviceId.Type.ADVERTISING_ID);
    }

    private class InAppMessageLoadListener implements InAppMessageListener {
        private Boolean wasLoaded = null;

        public boolean getWasLoaded() throws Exception {
            if (wasLoaded == null)
                throw new Exception();
            return wasLoaded;
        }

        @Override
        public void inAppMessageClicked(String messageId, InAppMessage inAppMessage) {}

        @Override
        public void inAppMessageClosed(String messageId, InAppMessage inAppMessage, boolean completed) {}

        @Override
        public void inAppMessageLoadSucceeded(String messageId, InAppMessage inAppMessage) {
            synchronized (this) {
                wasLoaded = true;
                notifyAll();
            }
        }

        @Override
        public void inAppMessageShown(String messageId, InAppMessage inAppMessage, boolean succeeded) {}

        @Override
        public void noInAppMessageFound(String messageId) {
            synchronized (this) {
                wasLoaded = false;
                notifyAll();
            }
        }
    }

    @Test
    public void testSeedInAppMessageLoadSucceeded() throws Exception {
        InAppMessageLoadListener listener = new InAppMessageLoadListener();

        seeds.init(context, null, listener, SERVER, UNLIMITED_ADS_APP_KEY);
        seeds.requestInAppMessage();
        synchronized (listener) {
            listener.wait(50000);
        }
        Assert.assertTrue(listener.getWasLoaded());
    }

    @Test
    public void testSeedInAppMessageLoadFailed() throws Exception {
        InAppMessageLoadListener listener = new InAppMessageLoadListener();

        seeds.init(context, null, listener, SERVER, NO_ADS_APP_KEY);
        seeds.requestInAppMessage();
        synchronized (listener) {
            listener.wait(50000);
        }
        Assert.assertFalse(listener.getWasLoaded());
    }
}