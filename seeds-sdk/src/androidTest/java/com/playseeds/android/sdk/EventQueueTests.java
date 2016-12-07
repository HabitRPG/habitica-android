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

import android.test.AndroidTestCase;

import org.mockito.ArgumentCaptor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventQueueTests extends AndroidTestCase {
    EventQueue mEventQueue;
    CountlyStore mMockCountlyStore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mMockCountlyStore = mock(CountlyStore.class);
        mEventQueue = new EventQueue(mMockCountlyStore);
    }

    public void testConstructor() {
        assertSame(mMockCountlyStore, mEventQueue.getCountlyStore());
    }

    public void testRecordEvent() {
        final String eventKey = "eventKey";
        final int count = 42;
        final double sum = 3.0d;
        final Map<String, String> segmentation = new HashMap<>(1);
        final int timestamp = Seeds.currentTimestamp();
        final ArgumentCaptor<Integer> arg = ArgumentCaptor.forClass(Integer.class);

        mEventQueue.recordEvent(eventKey, segmentation, count, sum);
        verify(mMockCountlyStore).addEvent(eq(eventKey), eq(segmentation), arg.capture(), eq(count), eq(sum));
        assertTrue(((timestamp - 1) <= arg.getValue()) && ((timestamp + 1) >= arg.getValue()));
    }

    public void testSize_zeroLenArray() {
        when(mMockCountlyStore.events()).thenReturn(new String[0]);
        assertEquals(0, mEventQueue.size());
    }

    public void testSize() {
        when(mMockCountlyStore.events()).thenReturn(new String[2]);
        assertEquals(2, mEventQueue.size());
    }

    public void testEvents_emptyList() throws UnsupportedEncodingException {
        final List<Event> eventsList = new ArrayList<>();
        when(mMockCountlyStore.eventsList()).thenReturn(eventsList);

        final String expected = URLEncoder.encode("[]", "UTF-8");
        assertEquals(expected, mEventQueue.events());
        verify(mMockCountlyStore).eventsList();
        verify(mMockCountlyStore).removeEvents(eventsList);
    }

    public void testEvents_nonEmptyList() throws UnsupportedEncodingException {
        final List<Event> eventsList = new ArrayList<>();
        final Event event1 = new Event();
        event1.key = "event1Key";
        eventsList.add(event1);
        final Event event2 = new Event();
        event2.key = "event2Key";
        eventsList.add(event2);
        when(mMockCountlyStore.eventsList()).thenReturn(eventsList);

        final String jsonToEncode = "[" + event1.toJSON().toString() + "," + event2.toJSON().toString() + "]";
        final String expected = URLEncoder.encode(jsonToEncode, "UTF-8");
        assertEquals(expected, mEventQueue.events());
        verify(mMockCountlyStore).eventsList();
        verify(mMockCountlyStore).removeEvents(eventsList);
    }
}
