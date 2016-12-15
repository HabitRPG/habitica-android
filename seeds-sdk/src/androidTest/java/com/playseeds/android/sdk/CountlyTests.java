/*
Copyright (c) 2012, 2013, 2014 Countly

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.playseeds.android.sdk;

import android.content.Context;
import android.test.AndroidTestCase;

import java.util.HashMap;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class CountlyTests extends AndroidTestCase {
    Seeds mUninitedSeeds;
    Seeds mSeeds;
    ConnectionQueue mockConnectionQueue;
    EventQueue mockEventQueue;
    String eventKey;
    Context context;
    String serverUrl;
    String appKey;
    String deviceId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        context = getContext();
        serverUrl = "http://ly.count.android.sdk.test.count.ly";
        appKey = "appkey";
        deviceId = "1234";
        mUninitedSeeds = new Seeds();
        mSeeds = new Seeds();
        mockEventQueue = mock(EventQueue.class);
        mockConnectionQueue = mock(ConnectionQueue.class);
        final CountlyStore countlyStore = new CountlyStore(context);
        countlyStore.clear();

        mSeeds.init(context, null, null, serverUrl, appKey, deviceId);
        mockConnectionQueue.setContext(context);
        mockConnectionQueue.setAppKey("123456");
        mockConnectionQueue.setServerURL(serverUrl);
        mockConnectionQueue.setCountlyStore(mock(CountlyStore.class));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor() {
        assertNotNull(mUninitedSeeds.getConnectionQueue());
        assertNull(mUninitedSeeds.getConnectionQueue().getContext());
        assertNull(mUninitedSeeds.getConnectionQueue().getServerURL());
        assertNull(mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getTimerService());
        assertNull(mUninitedSeeds.getEventQueue());
        assertEquals(0, mUninitedSeeds.getActivityCount());
        assertEquals(0, mUninitedSeeds.getPrevSessionDurationStartTime());
        assertFalse(mUninitedSeeds.getDisableUpdateSessionRequests());
        assertFalse(mUninitedSeeds.isLoggingEnabled());
    }

    public void testSharedInstance() {
        Seeds sharedSeeds = Seeds.sharedInstance();
        assertNotNull(sharedSeeds);
        assertSame(sharedSeeds, Seeds.sharedInstance());
    }

    public void testInitWithNoDeviceID() {
        mUninitedSeeds = spy(mUninitedSeeds);
        mUninitedSeeds.init(context, null, null, serverUrl, appKey, null);
        verify(mUninitedSeeds).init(context, null, null, serverUrl, appKey, null);
    }

    public void testInit_nullContext() {
        try {
            mUninitedSeeds.init(null, null, null, serverUrl, appKey, deviceId);
            fail("expected null context to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullServerURL() {
        try {
            mUninitedSeeds.init(context, null, null, appKey, deviceId);
            fail("expected null server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_emptyServerURL() {
        try {
            mUninitedSeeds.init(context, null, null, "", appKey, deviceId);
            fail("expected empty server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_invalidServerURL() {
        try {
            mUninitedSeeds.init(context, null, null, "not-a-valid-server-url", appKey, deviceId);
            fail("expected invalid server URL to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullAppKey() {
        try {
            mUninitedSeeds.init(context, null, null, serverUrl, null, deviceId);
            fail("expected null app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_emptyAppKey() {
        try {
            mUninitedSeeds.init(context, null, null, serverUrl, "", deviceId);
            fail("expected empty app key to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_nullDeviceID() {
        // null device ID is okay because it tells Seeds to use OpenUDID
        mUninitedSeeds.init(context, null, null, serverUrl, appKey, null);
    }

    public void testInit_emptyDeviceID() {
        try {
            mUninitedSeeds.init(context, null, null, serverUrl, appKey, "");
            fail("expected empty device ID to throw IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithSameParams() {
        mUninitedSeeds.init(context, null, null, serverUrl, appKey, deviceId);

        final EventQueue expectedEventQueue = mUninitedSeeds.getEventQueue();
        final ConnectionQueue expectedConnectionQueue = mUninitedSeeds.getConnectionQueue();
        final CountlyStore expectedCountlyStore = expectedConnectionQueue.getCountlyStore();
        assertNotNull(expectedEventQueue);
        assertNotNull(expectedConnectionQueue);
        assertNotNull(expectedCountlyStore);

        // second call with same params should succeed, no exception thrown
        mUninitedSeeds.init(getContext(), null, null, serverUrl, appKey, deviceId);

        assertSame(expectedEventQueue, mUninitedSeeds.getEventQueue());
        assertSame(expectedConnectionQueue, mUninitedSeeds.getConnectionQueue());
        assertSame(expectedCountlyStore, mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertSame(context, mUninitedSeeds.getConnectionQueue().getContext());
        assertEquals(serverUrl, mUninitedSeeds.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedSeeds.getConnectionQueue().getAppKey());
        assertSame(mUninitedSeeds.getConnectionQueue().getCountlyStore(), mUninitedSeeds.getEventQueue().getCountlyStore());
    }

    public void testInit_twiceWithDifferentContext() {
        mUninitedSeeds.init(context, null, null, serverUrl, appKey, deviceId);
        // changing context is okay since SharedPrefs are global singletons
        try {
            mUninitedSeeds.init(mock(Context.class), null, null, serverUrl, appKey, deviceId);
        } catch (Exception e) {
            //success!
        }
    }

    public void testInit_twiceWithDifferentServerURL() {
        mUninitedSeeds.init(context, null, null, "http://test1.count.ly", appKey, deviceId);
        try {
            mUninitedSeeds.init(context, null, null, "http://test2.count.ly", appKey, deviceId);
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithDifferentAppKey() {
        mUninitedSeeds.init(context, null, null, serverUrl, "appkey1", deviceId);
        try {
            mUninitedSeeds.init(context, null, null, serverUrl, "appkey2", deviceId);
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_twiceWithDifferentDeviceID() {
        mUninitedSeeds.init(context, null, null, serverUrl, appKey, deviceId);
        try {
            mUninitedSeeds.init(context, null, null, serverUrl, appKey, "4321");
            fail("expected IllegalStateException to be thrown when calling init a second time with different serverURL");
        }
        catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testInit_normal() {
        mUninitedSeeds.init(getContext(), null, null, serverUrl, appKey, deviceId);

        assertSame(getContext(), mUninitedSeeds.getConnectionQueue().getContext());
        assertEquals(serverUrl, mUninitedSeeds.getConnectionQueue().getServerURL());
        assertEquals(appKey, mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNotNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getEventQueue());
        assertSame(mUninitedSeeds.getConnectionQueue().getCountlyStore(), mUninitedSeeds.getEventQueue().getCountlyStore());
    }

    public void testHalt_notInitialized() {
        mUninitedSeeds.halt();
        assertNotNull(mUninitedSeeds.getConnectionQueue());
        assertNull(mUninitedSeeds.getConnectionQueue().getContext());
        assertNull(mUninitedSeeds.getConnectionQueue().getServerURL());
        assertNull(mUninitedSeeds.getConnectionQueue().getAppKey());
        assertNull(mUninitedSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mUninitedSeeds.getTimerService());
        assertNull(mUninitedSeeds.getEventQueue());
        assertEquals(0, mUninitedSeeds.getActivityCount());
        assertEquals(0, mUninitedSeeds.getPrevSessionDurationStartTime());
    }

    public void testHalt() {
        final CountlyStore mockCountlyStore = mock(CountlyStore.class);
        mSeeds.getConnectionQueue().setCountlyStore(mockCountlyStore);
        mSeeds.onStart();
        assertTrue(0 != mSeeds.getPrevSessionDurationStartTime());
        assertTrue(0 != mSeeds.getActivityCount());
        assertNotNull(mSeeds.getEventQueue());
        assertNotNull(mSeeds.getConnectionQueue().getContext());
        assertNotNull(mSeeds.getConnectionQueue().getServerURL());
        assertNotNull(mSeeds.getConnectionQueue().getAppKey());
        assertNotNull(mSeeds.getConnectionQueue().getContext());

        mSeeds.halt();

        assertNotNull(mSeeds.getConnectionQueue());
        assertNull(mSeeds.getConnectionQueue().getContext());
        assertNull(mSeeds.getConnectionQueue().getServerURL());
        assertNull(mSeeds.getConnectionQueue().getAppKey());
        assertNull(mSeeds.getConnectionQueue().getCountlyStore());
        assertNotNull(mSeeds.getTimerService());
        assertNull(mSeeds.getEventQueue());
        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testOnStart_initNotCalled() {
        try {
            mUninitedSeeds.onStart();
            fail("expected calling onStart before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStart_firstCall() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.onStart();

        assertEquals(1, mSeeds.getActivityCount());
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        assertTrue(prevSessionDurationStartTime > 0);
        assertTrue(prevSessionDurationStartTime <= System.nanoTime());
    }

    public void testOnStart_subsequentCall() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.onStart(); // first call to onStart
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        mSeeds.onStart(); // second call to onStart

        assertEquals(2, mSeeds.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testOnStop_initNotCalled() {
        try {
            mUninitedSeeds.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStop_unbalanced() {
        try {
            mSeeds.onStop();
            fail("expected calling onStop before init to throw IllegalStateException");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testOnStop_reallyStopping_emptyEventQueue() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.onStart();
        mSeeds.onStop();

        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testOnStop_reallyStopping_nonEmptyEventQueue() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onStart();
        mSeeds.onStop();

        assertEquals(0, mSeeds.getActivityCount());
        assertEquals(0, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testOnStop_notStopping() {
        mSeeds.setConnectionQueue(mockConnectionQueue);

        mSeeds.onStart();
        mSeeds.onStart();
        final long prevSessionDurationStartTime = mSeeds.getPrevSessionDurationStartTime();
        mSeeds.onStop();

        assertEquals(1, mSeeds.getActivityCount());
        assertEquals(prevSessionDurationStartTime, mSeeds.getPrevSessionDurationStartTime());
    }

    public void testRecordEvent_keyOnly() {
        eventKey = "eventKey";
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, 1, 0.0d);
        seeds.recordEvent(eventKey);
    }

    public void testRecordEvent_keyAndCount() {
        eventKey = "eventKey";
        final int count = 42;
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, count, 0.0d);
        seeds.recordEvent(eventKey, count);
    }

    public void testRecordEvent_keyAndCountAndSum() {
        eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, null, count, sum);
        seeds.recordEvent(eventKey, count, sum);
    }

    public void testRecordEvent_keyAndSegmentationAndCount() {
        eventKey = "eventKey";
        final int count = 42;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");
        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).recordEvent(eventKey, segmentation, count, 0.0d);
        seeds.recordEvent(eventKey, segmentation, count);
    }

    public void testRecordEvent_initNotCalled() {
        eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mUninitedSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalStateException when recordEvent called before init");
        } catch (IllegalStateException ignored) {
            // success
        }
    }

    public void testRecordEvent_nullKey() {
        eventKey = null;
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            //noinspection ConstantConditions
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_emptyKey() {
        eventKey = "";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_countIsZero() {
        eventKey = "";
        final int count = 0;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with count=0");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_countIsNegative() {
        eventKey = "";
        final int count = -1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with a negative count");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasNullKey() {
        eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put(null, "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasEmptyKey() {
        eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("", "segvalue1");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty key");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasNullValue() {
        eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", null);

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with null value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent_segmentationHasEmptyValue() {
        eventKey = "";
        final int count = 1;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "");

        try {
            mSeeds.recordEvent(eventKey, segmentation, count, sum);
            fail("expected IllegalArgumentException when recordEvent called with segmentation with empty value");
        } catch (IllegalArgumentException ignored) {
            // success
        }
    }

    public void testRecordEvent() {
        eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final HashMap<String, String> segmentation = new HashMap<>(1);
        segmentation.put("segkey1", "segvalue1");

        mSeeds.setEventQueue(mockEventQueue);

        final Seeds seeds = spy(mSeeds);
        doNothing().when(seeds).sendEventsIfNeeded();
        seeds.recordEvent(eventKey, segmentation, count, sum);
    }

    public void testSendEventsIfNeeded_emptyQueue() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.sendEventsIfNeeded();
    }

    public void testSendEventsIfNeeded_lessThanThreshold() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.sendEventsIfNeeded();
    }

    public void testSendEventsIfNeeded_equalToThreshold() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.sendEventsIfNeeded();
    }

    public void testSendEventsIfNeeded_moreThanThreshold() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.sendEventsIfNeeded();
    }

    public void testOnTimer_noActiveSession() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onTimer();
    }

    public void testOnTimer_activeSession_emptyEventQueue() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onStart();
        mSeeds.onTimer();
    }

    public void testOnTimer_activeSession_nonEmptyEventQueue() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onStart();
        mSeeds.onTimer();
    }

    public void testOnTimer_activeSession_emptyEventQueue_sessionTimeUpdatesDisabled() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setDisableUpdateSessionRequests(true);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onStart();
        mSeeds.onTimer();
    }

    public void testOnTimer_activeSession_nonEmptyEventQueue_sessionTimeUpdatesDisabled() {
        mSeeds.setConnectionQueue(mockConnectionQueue);
        mSeeds.setDisableUpdateSessionRequests(true);
        mSeeds.setEventQueue(mockEventQueue);
        mSeeds.onStart();
        mSeeds.onTimer();
    }

    public void testRoundedSecondsSinceLastSessionDurationUpdate() {
        long prevSessionDurationStartTime = System.nanoTime() - 1000000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 2000000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1600000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(2, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());

        prevSessionDurationStartTime = System.nanoTime() - 1200000000;
        mSeeds.setPrevSessionDurationStartTime(prevSessionDurationStartTime);
        assertEquals(1, mSeeds.roundedSecondsSinceLastSessionDurationUpdate());
    }

    public void testIsValidURL_badURLs() {
        assertFalse(Seeds.isValidURL(null));
        assertFalse(Seeds.isValidURL(""));
        assertFalse(Seeds.isValidURL(" "));
        assertFalse(Seeds.isValidURL("blahblahblah.com"));
    }

    public void testIsValidURL_goodURL() {
        assertTrue(Seeds.isValidURL(serverUrl));
    }

    public void testCurrentTimestamp() {
        final int testTimestamp = (int) (System.currentTimeMillis() / 1000l);
        final int actualTimestamp = Seeds.currentTimestamp();
        assertTrue(((testTimestamp - 1) <= actualTimestamp) && ((testTimestamp + 1) >= actualTimestamp));
    }

    public void testSetDisableUpdateSessionRequests() {
        assertFalse(mSeeds.getDisableUpdateSessionRequests());
        mSeeds.setDisableUpdateSessionRequests(true);
        assertTrue(mSeeds.getDisableUpdateSessionRequests());
        mSeeds.setDisableUpdateSessionRequests(false);
        assertFalse(mSeeds.getDisableUpdateSessionRequests());
    }

    public void testLoggingFlag() {
        assertFalse(mUninitedSeeds.isLoggingEnabled());
        mUninitedSeeds.setLoggingEnabled(true);
        assertTrue(mUninitedSeeds.isLoggingEnabled());
        mUninitedSeeds.setLoggingEnabled(false);
        assertFalse(mUninitedSeeds.isLoggingEnabled());
    }
}