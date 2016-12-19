package com.playseeds.android.sdk;

import android.app.Activity;
import android.content.Context;
import android.test.AndroidTestCase;

import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


public class SeedsTest extends AndroidTestCase {
    Seeds seeds;
    Seeds seedsSpy;
    Context context;
    ConnectionQueue mockQueue;

    public void setUp() throws Exception {
        seeds = Seeds.sharedInstance();
        seedsSpy = spy(seeds);
        context = getContext();
        mockQueue = mock(ConnectionQueue.class);
        ExecutorService mockService = mock(ExecutorService.class);

        mockQueue.setContext(context);
        mockQueue.setAppKey("fake key");
        mockQueue.setServerURL("https://devdash.com");
        mockQueue.setCountlyStore(mock(CountlyStore.class));
        mockQueue.setExecutor(mockService);
    }

    public void testInitMessaging() throws Exception {
        try {
            seeds.initMessaging(new Activity(), testActivity.class, null, Seeds.CountlyMessagingMode.TEST);
            fail("IllegalStateException expected!");
        } catch(IllegalStateException e) {
            // success
        }
    }

    public void testInitMessaging_WhenNullMode() throws Exception {
        try {
            seeds.initMessaging(new Activity(), testActivity.class, null, null);
            fail("IllegalStateException expected");
        } catch(IllegalStateException e) {
            // success
        }
    }

    public void testOnRegistrationId() throws Exception {
        seedsSpy.setConnectionQueue(mockQueue);
        doCallRealMethod().when(seedsSpy).onRegistrationId("fake id");
        seedsSpy.onRegistrationId("fake id");

        verify(seedsSpy).onRegistrationId("fake id");
    }

    public void testTrackPurchase() throws Exception {
        seedsSpy.init(context, null, mock(InAppMessageListener.class), "https://devdash.com", "fake key", "Nexus");
        doCallRealMethod().when(seedsSpy).trackPurchase("fake key", 1.00);
        seedsSpy.trackPurchase("fake key", 1.00);

        verify(seedsSpy).trackPurchase("fake key", 1.00);
        verify(seedsSpy).recordIAPEvent("fake key", 1.00);
    }

    public void testSetUserData() throws Exception {
        Map<String, String> data = new HashMap<>();
        doCallRealMethod().when(seedsSpy).setUserData(data, data);
        seedsSpy.setConnectionQueue(mockQueue);
        seedsSpy.setUserData(data, data);

        verify(seedsSpy).setUserData(data, data);
    }

    public void testSetCustomUserData() throws Exception {
        Map<String, String> data = new HashMap<>();
        doCallRealMethod().when(seedsSpy).setCustomUserData(data);
        seedsSpy.setConnectionQueue(mockQueue);
        seedsSpy.setCustomUserData(data);

        verify(seedsSpy).setCustomUserData(data);
    }

    public void testSetLocation() throws Exception {
        doCallRealMethod().when(seedsSpy).setLocation(2.34, 3.45);
        seedsSpy.setConnectionQueue(mockQueue);
        seedsSpy.setLocation(2.34, 3.45);

        verify(seedsSpy).setLocation(2.34, 3.45);
    }

    public void testCustomCrashSegments() throws Exception {
        Map<String, String> data = new HashMap<>();
        doCallRealMethod().when(seedsSpy).setCustomCrashSegments(data);
        seedsSpy.setCustomCrashSegments(data);

        verify(seedsSpy).setCustomCrashSegments(data);
    }

    public void testAddCrashLog() throws Exception {
        doCallRealMethod().when(seedsSpy).addCrashLog("fake crash");
        seedsSpy.addCrashLog("fake crash");

        verify(seedsSpy).addCrashLog("fake crash");
    }

    public void testLogException() throws Exception {
        Exception e = new Exception();
        doCallRealMethod().when(seedsSpy).logException(e);
        seedsSpy.setConnectionQueue(mockQueue);
        seedsSpy.logException(e);

        verify(seedsSpy).logException(e);
    }

    public void testLogExceptionWhenString() throws Exception {
        doCallRealMethod().when(seedsSpy).logException("Exception");
        seedsSpy.setConnectionQueue(mockQueue);
        seedsSpy.logException("Exception");

        verify(seedsSpy).logException("Exception");
    }

    public void testEnableCrashReporting() throws Exception {
        doCallRealMethod().when(seedsSpy).enableCrashReporting();
        seedsSpy.enableCrashReporting();

        verify(seedsSpy).enableCrashReporting();
    }

    public class testActivity extends Activity {
    }
}
