package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;

import com.playseeds.android.sdk.DeviceId;

import java.util.ArrayList;
import java.util.Date;

public class InAppMessageRequestTest  extends AndroidTestCase {

    public void testGettersAndSetters() throws Exception {
        InAppMessageRequest request = new InAppMessageRequest();
        long timeStamp = new Date().getTime();

        request.setDeviceId("fake id");
        request.setGender(Gender.FEMALE);
        request.setUserAge(30);
        request.setKeywords(new ArrayList<String>());
        request.setIdMode(DeviceId.Type.ADVERTISING_ID);
        request.setConnectionType("fake connection type");
        request.setAdDoNotTrack(true);
        request.setOrientation("landscape");
        request.setHeaders("fake headers");
        request.setIpAddress("fake address");
        request.setLatitude(23.43);
        request.setListAds("fake list ads");
        request.setLongitude(34.34);
        request.setProtocolVersion("fake protocol version");
        request.setAppKey("fake app key");
        request.setTimestamp(timeStamp);
        request.setUserAgent("fake user agent");
        request.setUserAgent2("fake user agent 2");
        request.setRequestURL("fake Url");
        request.setAdspaceStrict(true);
        request.setAdspaceHeight(10);
        request.setAdspaceWidth(10);
        request.setAndroidAdId("fake android id");

        assertSame("fake id", request.getDeviceId());
        assertSame(Gender.FEMALE, request.getGender());
        assertEquals(30, request.getUserAge());
        assertNotNull(request.getKeywords());
        assertSame(DeviceId.Type.ADVERTISING_ID, request.getIdMode());
        assertSame("fake connection type", request.getConnectionType());
        assertTrue(request.hasAdDoNotTrack());
        assertSame("landscape", request.getOrientation());
        assertSame("fake headers", request.getHeaders());
        assertSame("fake address", request.getIpAddress());
        assertEquals(23.43, request.getLatitude());
        assertSame("fake list ads", request.getListAds());
        assertEquals(34.34, request.getLongitude());
        assertSame("fake protocol version", request.getProtocolVersion());
        assertSame("fake app key", request.getAppKey());
        assertEquals(timeStamp, request.getTimestamp());
        assertSame("fake user agent", request.getUserAgent());
        assertSame("fake user agent 2", request.getUserAgent2());
        assertSame("fake Url", request.getRequestURL());
        assertTrue(request.isAdspaceStrict());
        assertEquals(10, request.getAdspaceHeight());
        assertEquals(10, request.getAdspaceWidth());
        assertSame("fake android id", request.getAndroidAdId());
        assertNotNull(request.countlyUriToString());
        assertNotNull(request.toCountlyUri());
    }
}
