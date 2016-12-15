package com.playseeds.android.sdk.inappmessaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.test.AndroidTestCase;

public class UtilTest extends AndroidTestCase {
    Context context;

    public void setUp() throws Exception {
        context = getContext();
    }

    public void testIsNetworkAvailable() throws Exception {
        boolean isAvailable = Util.isNetworkAvailable(context);
        assertNotNull(isAvailable);
        assertTrue(isAvailable);
    }

    public void testGetConnectionType() throws Exception {
        String connectionType = Util.getConnectionType(context);
        assertNotNull(connectionType);
    }

    public void testGetLocalIpAddress() throws Exception {
        String localIpAddress = Util.getLocalIpAddress();
        assertNotNull(localIpAddress);
    }

    public void testGetLocation() throws Exception {
        Location location = Util.getLocation(context);
        assertNull(location);
    }

    public void testGetDefaultUserAgentString() throws Exception{
        String userAgent = Util.getDefaultUserAgentString();
        assertNotNull(userAgent);
    }

    public void testBuildUserAgent() throws Exception {
        String userAgent = Util.buildUserAgent();
        assertNotNull(userAgent);
    }

    public void testGetMemoryClass() throws Exception {
        int memoryClass = Util.getMemoryClass(context);
        assertNotNull(memoryClass);
    }

    public void testGetAndroidAdId() throws Exception {
        String androidAdId = Util.getAndroidAdId();
        assertNotNull(androidAdId);
    }

    public void testLoadBitMap() throws Exception {
        Bitmap bitmap = Util.loadBitmap("http://devdash.playseeds.com");
        assertNull(bitmap);
    }
}
