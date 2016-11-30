package com.playseeds.android.sdk.inappmessaging;

import android.os.Message;
import android.test.AndroidTestCase;
import android.view.KeyEvent;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class RichMediaActivityTest extends AndroidTestCase {
    RichMediaActivity mockRichMediaActivity;
    InAppMessageResponse mockResponse;
    RichMediaActivity spyActivity;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mockRichMediaActivity = mock(RichMediaActivity.class);
        mockResponse = mock(InAppMessageResponse.class);
        spyActivity = new RichMediaActivity();
        spyActivity = spy(spyActivity);
    }

    public void testFinish() throws Exception {
        doCallRealMethod().when(mockRichMediaActivity).finish();
        mockRichMediaActivity.finish();

        verify(mockRichMediaActivity).finish();
    }

    public void testOnKeyDown_WithMockMethod() throws Exception {
        KeyEvent keyEvent = mock(KeyEvent.class);
        boolean isKey;

        when(mockRichMediaActivity.onKeyDown(23, keyEvent)).thenReturn(true);
        isKey = mockRichMediaActivity.onKeyDown(23, keyEvent);
        assertTrue(isKey);
        verify(mockRichMediaActivity).onKeyDown(23, keyEvent);

        when(mockRichMediaActivity.onKeyDown(24, keyEvent)).thenReturn(false);
        isKey = mockRichMediaActivity.onKeyDown(24, keyEvent);
        assertFalse(isKey);
        verify(mockRichMediaActivity).onKeyDown(24, keyEvent);
    }

    public void testKeyDown_WithActualMethodCall() throws Exception {
        KeyEvent keyEvent = mock(KeyEvent.class);
        boolean isKey;

        doCallRealMethod().when(mockRichMediaActivity).onKeyDown(keyEvent.KEYCODE_BACK, keyEvent);
        isKey = mockRichMediaActivity.onKeyDown(keyEvent.KEYCODE_BACK, keyEvent);
        assertTrue(isKey);
        verify(mockRichMediaActivity).onKeyDown(keyEvent.KEYCODE_BACK, keyEvent);
        verify(mockRichMediaActivity).goBack();
    }

    public void testOnResume() throws Exception {
        doNothing().when(spyActivity).onResume();
        spyActivity.onResume();

        verify(spyActivity).onResume();
    }

    public void testOnDestroy() throws Exception {
        doNothing().when(spyActivity).onDestroy();
        spyActivity.onDestroy();

        verify(spyActivity).onDestroy();
    }

    public void testClose() throws Exception {
        doCallRealMethod().when(mockRichMediaActivity).close();
        mockRichMediaActivity.close();

        verify(mockRichMediaActivity).close();
        verify(mockRichMediaActivity).finish();
    }

    public void testHandleMessage() throws Exception {
        Message message = new Message();
        doCallRealMethod().when(mockRichMediaActivity).handleMessage(message);
        mockRichMediaActivity.handleMessage(message);

        verify(mockRichMediaActivity).handleMessage(message);
    }

    public void testGoBack_WhenTypeBrowser() throws Exception {
        doCallRealMethod().when(mockRichMediaActivity).goBack();
        doCallRealMethod().when(mockRichMediaActivity).setTypeInterstitial(0);
        mockRichMediaActivity.setTypeInterstitial(0);
        mockRichMediaActivity.goBack();

        verify(mockRichMediaActivity).goBack();
        verify(mockRichMediaActivity).finish();
    }

    public void testGoBack_WhenTypeInterstitial() throws Exception {
        doCallRealMethod().when(mockRichMediaActivity).goBack();
        doCallRealMethod().when(mockRichMediaActivity).setTypeInterstitial(2);
        mockRichMediaActivity.setTypeInterstitial(2);
        mockRichMediaActivity.goBack();

        verify(mockRichMediaActivity).goBack();
        verify(mockRichMediaActivity).finish();
    }

    public void testOnCreate() throws Exception {
        doNothing().when(spyActivity).onCreate(null);
        spyActivity.onCreate(null);

        verify(spyActivity).onCreate(null);
    }
}

