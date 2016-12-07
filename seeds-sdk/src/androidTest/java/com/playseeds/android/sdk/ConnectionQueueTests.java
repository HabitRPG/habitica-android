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

import android.net.Uri;
import android.test.AndroidTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ConnectionQueueTests extends AndroidTestCase {
    ConnectionQueue connQ;
    ConnectionQueue freshConnQ;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        freshConnQ = new ConnectionQueue();
        connQ = new ConnectionQueue();
        connQ.setAppKey("abcDeFgHiJkLmNoPQRstuVWxyz");
        connQ.setServerURL("http://countly.coupons.com");
        connQ.setContext(getContext());
        connQ.setCountlyStore(mock(CountlyStore.class));
        connQ.setDeviceId(mock(DeviceId.class));
        connQ.setExecutor(mock(ExecutorService.class));
    }

    public void testConstructor() {
        assertNull(freshConnQ.getCountlyStore());
        assertNull(freshConnQ.getDeviceId());
        assertNull(freshConnQ.getAppKey());
        assertNull(freshConnQ.getContext());
        assertNull(freshConnQ.getServerURL());
        assertNull(freshConnQ.getExecutor());
    }

    public void testAppKey() {
        final String appKey = "blahblahblah";
        freshConnQ.setAppKey(appKey);
        assertEquals(appKey, freshConnQ.getAppKey());
    }

    public void testContext() {
        freshConnQ.setContext(getContext());
        assertSame(getContext(), freshConnQ.getContext());
    }

    public void testServerURL() {
        final String serverURL = "http://countly.coupons.com";
        freshConnQ.setServerURL(serverURL);
        assertEquals(serverURL, freshConnQ.getServerURL());
    }

    public void testCountlyStore() {
        final CountlyStore store = new CountlyStore(getContext());
        freshConnQ.setCountlyStore(store);
        assertSame(store, freshConnQ.getCountlyStore());
    }

    public void testDeviceId() {
        final DeviceId deviceId = new DeviceId("blah");
        freshConnQ.setDeviceId(deviceId);
        assertSame(deviceId, freshConnQ.getDeviceId());
    }

    public void testExecutor() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        freshConnQ.setExecutor(executor);
        assertSame(executor, freshConnQ.getExecutor());
    }

    public void testCheckInternalState_nullAppKey() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setAppKey(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testCheckInternalState_emptyAppKey() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setAppKey("");
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testCheckInternalState_nullStore() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setCountlyStore(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testCheckInternalState_nullContext() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setContext(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testCheckInternalState_nullServerURL() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setServerURL(null);
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testCheckInternalState_invalidServerURL() {
        connQ.checkInternalState(); // shouldn't throw
        connQ.setServerURL("blahblahblah.com");
        try {
            freshConnQ.checkInternalState();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testBeginSession_checkInternalState() {
        try {
            freshConnQ.beginSession();
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testBeginSession() throws JSONException, UnsupportedEncodingException {
        connQ.beginSession();
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp-1) <= actualTimestamp) && ((curTimestamp+1) >= actualTimestamp));
        assertEquals(Seeds.COUNTLY_SDK_VERSION_STRING, queryParams.get("sdk_version"));
        assertEquals("1", queryParams.get("begin_session"));
        // validate metrics
        final JSONObject actualMetrics = new JSONObject(queryParams.get("metrics"));
        final String metricsJsonStr = URLDecoder.decode(DeviceInfo.getMetrics(getContext()), "UTF-8");
        final JSONObject expectedMetrics = new JSONObject(metricsJsonStr);
        assertEquals(expectedMetrics.length(), actualMetrics.length());
        final Iterator actualMetricsKeyIterator = actualMetrics.keys();
        while (actualMetricsKeyIterator.hasNext()) {
            final String key = (String) actualMetricsKeyIterator.next();
            assertEquals(expectedMetrics.get(key), actualMetrics.get(key));
        }
    }

    public void testUpdateSession_checkInternalState() {
        try {
            freshConnQ.updateSession(15);
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testUpdateSession_zeroDuration() {
        connQ.updateSession(0);
        verifyZeroInteractions(connQ.getExecutor(), connQ.getCountlyStore());
    }

    public void testUpdateSession_negativeDuration() {
        connQ.updateSession(-1);
        verifyZeroInteractions(connQ.getExecutor(), connQ.getCountlyStore());
    }

    public void testUpdateSession_moreThanZeroDuration() {
        connQ.updateSession(60);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp-1) <= actualTimestamp) && ((curTimestamp+1) >= actualTimestamp));
        assertEquals("60", queryParams.get("session_duration"));
    }

    public void testEndSession_checkInternalState() {
        try {
            freshConnQ.endSession(15);
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testEndSession_zeroDuration() {
        connQ.endSession(0);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp-1) <= actualTimestamp) && ((curTimestamp+1) >= actualTimestamp));
        assertFalse(queryParams.containsKey("session_duration"));
        assertEquals("1", queryParams.get("end_session"));
    }

    public void testEndSession_negativeDuration() {
        connQ.endSession(-1);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp-1) <= actualTimestamp) && ((curTimestamp+1) >= actualTimestamp));
        assertFalse(queryParams.containsKey("session_duration"));
        assertEquals("1", queryParams.get("end_session"));
    }

    public void testEndSession_moreThanZeroDuration() {
        connQ.endSession(15);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp-1) <= actualTimestamp) && ((curTimestamp+1) >= actualTimestamp));
        assertEquals("1", queryParams.get("end_session"));
        assertEquals("15", queryParams.get("session_duration"));
    }

    public void testRecordEvents_checkInternalState() {
        try {
            freshConnQ.recordEvents("blahblahblah");
            fail("expected IllegalStateException when internal state is not set up");
        } catch (IllegalStateException ignored) {
            // success!
        }
    }

    public void testRecordEvents() {
        final String eventData = "blahblahblah";
        connQ.recordEvents(eventData);
        final ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
        verify(connQ.getCountlyStore()).addConnection(arg.capture());
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));

        // verify query parameters
        final String queryStr = arg.getValue();
        final Map<String, String> queryParams = parseQueryParams(queryStr);
        assertEquals(connQ.getAppKey(), queryParams.get("app_key"));
        assertNull(queryParams.get("device_id"));
        final long curTimestamp = Seeds.currentTimestamp();
        final int actualTimestamp = Integer.parseInt(queryParams.get("timestamp"));
        // this check attempts to account for minor time changes during this ly.count.android.sdk.test
        assertTrue(((curTimestamp - 1) <= actualTimestamp) && ((curTimestamp + 1) >= actualTimestamp));
        assertEquals(eventData, queryParams.get("events"));
    }

    private Map<String, String> parseQueryParams(final String queryStr) {
        final String urlStr = "http://server?" + queryStr;
        final Uri uri = Uri.parse(urlStr);
        final Set<String> queryParameterNames = uri.getQueryParameterNames();
        final Map<String, String> queryParams = new HashMap<>(queryParameterNames.size());
        for (String paramName : queryParameterNames) {
            queryParams.put(paramName, uri.getQueryParameter(paramName));
        }
        return queryParams;
    }

    public void testEnsureExecutor_nullExecutor() {
        assertNull(freshConnQ.getExecutor());
        freshConnQ.ensureExecutor();
        assertNotNull(freshConnQ.getExecutor());
    }

    public void testEnsureExecutor_alreadyHasExecutor() {
        ExecutorService executor = connQ.getExecutor();
        assertNotNull(executor);
        connQ.ensureExecutor();
        assertSame(executor, connQ.getExecutor());
    }

    public void testTick_storeHasNoConnections() {
        when(connQ.getCountlyStore().isEmptyConnections()).thenReturn(true);
        connQ.tick();
        verifyZeroInteractions(connQ.getExecutor());
    }

    public void testTick_storeHasConnectionsAndFutureIsNull() {
        final Future mockFuture = mock(Future.class);
        when(connQ.getExecutor().submit(any(ConnectionProcessor.class))).thenReturn(mockFuture);
        connQ.tick();
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));
        assertSame(mockFuture, connQ.getConnectionProcessorFuture());
    }

    public void testTick_checkConnectionProcessor() {
        final ArgumentCaptor<Runnable> arg = ArgumentCaptor.forClass(Runnable.class);
        when(connQ.getExecutor().submit(arg.capture())).thenReturn(null);
        connQ.tick();
        assertEquals(((ConnectionProcessor)arg.getValue()).getServerURL(), connQ.getServerURL());
        assertSame(((ConnectionProcessor)arg.getValue()).getCountlyStore(), connQ.getCountlyStore());
    }

    public void testTick_storeHasConnectionsAndFutureIsDone() {
        final Future<?> mockFuture = mock(Future.class);
        when(mockFuture.isDone()).thenReturn(true);
        connQ.setConnectionProcessorFuture(mockFuture);
        final Future mockFuture2 = mock(Future.class);
        when(connQ.getExecutor().submit(any(ConnectionProcessor.class))).thenReturn(mockFuture2);
        connQ.tick();
        verify(connQ.getExecutor()).submit(any(ConnectionProcessor.class));
        assertSame(mockFuture2, connQ.getConnectionProcessorFuture());
    }

    public void testTick_storeHasConnectionsButFutureIsNotDone() {
        final Future<?> mockFuture = mock(Future.class);
        connQ.setConnectionProcessorFuture(mockFuture);
        connQ.tick();
        verifyZeroInteractions(connQ.getExecutor());
    }
}
