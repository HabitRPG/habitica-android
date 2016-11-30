package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;

import java.util.Date;

public class InAppMessageResponseTest extends AndroidTestCase {

    public void testGettersAndSetters() throws Exception {
        InAppMessageResponse response = new InAppMessageResponse();
        long timeStamp = new Date().getTime();

        response.setTimestamp(timeStamp);
        response.setClickType(ClickType.BROWSER);
        response.setBannerHeight(300);
        response.setBannerWidth(300);
        response.setClickUrl("fake url");
        response.setImageUrl("fake image url");
        response.setRefresh(60);
        response.setScale(true);
        response.setSkipPreflight(true);
        response.setText("fake text");
        response.setUrlType("fake url type");
        response.setType(0);
        response.setSkipOverlay(0);
        response.setHorizontalOrientationRequested(true);

        assertNotNull(response.getTimestamp());
        assertNotNull(response.getClickType());
        assertNotNull(response.getBannerHeight());
        assertNotNull(response.getBannerWidth());
        assertNotNull(response.getClickUrl());
        assertNotNull(response.getImageUrl());
        assertNotNull(response.getRefresh());
        assertNotNull(response.isScale());
        assertNotNull(response.isSkipPreflight());
        assertNotNull(response.getText());
        assertNotNull(response.getUrlType());
        assertNotNull(response.getType());
        assertNotNull(response.getSkipOverlay());
        assertNotNull(response.isHorizontalOrientationRequested());
        assertNotNull(response.getString());

        assertEquals(response.getTimestamp(), timeStamp);
        assertSame(ClickType.BROWSER, response.getClickType());
        assertEquals(300, response.getBannerHeight());
        assertEquals(300, response.getBannerWidth());
        assertSame("fake url", response.getClickUrl());
        assertSame("fake image url", response.getImageUrl());
        assertEquals(60, response.getRefresh());
        assertTrue(response.isScale());
        assertTrue(response.isSkipPreflight());
        assertSame("fake text", response.getText());
        assertSame("fake url type", response.getUrlType());
        assertEquals(0, response.getType());
        assertEquals(0, response.getSkipOverlay());
        assertTrue(response.isHorizontalOrientationRequested());
    }
}
