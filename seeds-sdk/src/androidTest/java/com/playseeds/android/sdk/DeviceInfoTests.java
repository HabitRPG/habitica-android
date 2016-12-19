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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.test.AndroidTestCase;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DeviceInfoTests extends AndroidTestCase {
    Context mockContext;

    protected void setUp() throws Exception {
        mockContext = mock(Context.class);
    }

    public void testGetOS() {
        Assert.assertEquals("Android", DeviceInfo.getOS());
    }

    public void testGetOSVersion() {
        assertEquals(android.os.Build.VERSION.RELEASE, DeviceInfo.getOSVersion());
    }

    public void testGetDevice() {
        assertEquals(android.os.Build.MODEL, DeviceInfo.getDevice());
    }

    public void testGetResolution() {
        final DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        final String expected = metrics.widthPixels + "x" + metrics.heightPixels;
        assertEquals(expected, DeviceInfo.getResolution(getContext()));
    }

    public void testGetResolution_getWindowManagerReturnsNull() {
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(null);
        assertEquals("", DeviceInfo.getResolution(mockContext));
    }

    public void testGetResolution_getDefaultDisplayReturnsNull() {
        final WindowManager mockWindowMgr = mock(WindowManager.class);

        when(mockWindowMgr.getDefaultDisplay()).thenReturn(null);
        when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(mockWindowMgr);
        assertEquals("", DeviceInfo.getResolution(mockContext));
    }

    private Context mockContextForTestingDensity(final int density) {
        final DisplayMetrics metrics = new DisplayMetrics();
        final Resources mockResources = mock(Resources.class);
        metrics.densityDpi = density;

        when(mockResources.getDisplayMetrics()).thenReturn(metrics);
        when(mockContext.getResources()).thenReturn(mockResources);
        return mockContext;
    }

    public void testGetDensity() {
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_LOW);
        assertEquals("LDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_MEDIUM);
        assertEquals("MDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_TV);
        assertEquals("TVDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_HIGH);
        assertEquals("HDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XHIGH);
        assertEquals("XHDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XXHIGH);
        assertEquals("XXHDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_XXXHIGH);
        assertEquals("XXXHDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(DisplayMetrics.DENSITY_400);
        assertEquals("XMHDPI", DeviceInfo.getDensity(mockContext));
        mockContext = mockContextForTestingDensity(0);
        assertEquals("", DeviceInfo.getDensity(mockContext));
    }

    public void testGetCarrier_nullTelephonyManager() {
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
        assertEquals("", DeviceInfo.getCarrier(mockContext));
    }

    public void testGetCarrier_nullNetOperator() {
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn(null);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("", DeviceInfo.getCarrier(mockContext));
    }

    public void testGetCarrier_emptyNetOperator() {
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn("");
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("", DeviceInfo.getCarrier(mockContext));
    }

    public void testGetCarrier() {
        final TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);

        when(mockTelephonyManager.getNetworkOperatorName()).thenReturn("Verizon");
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
        assertEquals("Verizon", DeviceInfo.getCarrier(mockContext));
    }

    public void testGetLocale() {
        final Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("ab", "CD"));
            assertEquals("ab_CD", DeviceInfo.getLocale());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    public void testGetAppVersion() throws PackageManager.NameNotFoundException {
        final PackageInfo pkgInfo = new PackageInfo();
        final String fakePkgName = "i.like.chicken";
        final PackageManager mockPkgMgr = mock(PackageManager.class);
        pkgInfo.versionName = "42.0";

        when(mockPkgMgr.getPackageInfo(fakePkgName, 0)).thenReturn(pkgInfo);
        when(mockContext.getPackageName()).thenReturn(fakePkgName);
        when(mockContext.getPackageManager()).thenReturn(mockPkgMgr);
        assertEquals("42.0", DeviceInfo.getAppVersion(mockContext));
    }

    public void testGetAppVersion_pkgManagerThrows() throws PackageManager.NameNotFoundException {
        final String fakePkgName = "i.like.chicken";
        final PackageManager mockPkgMgr = mock(PackageManager.class);

        when(mockPkgMgr.getPackageInfo(fakePkgName, 0)).thenThrow(new PackageManager.NameNotFoundException());
        when(mockContext.getPackageName()).thenReturn(fakePkgName);
        when(mockContext.getPackageManager()).thenReturn(mockPkgMgr);
        assertEquals("1.0", DeviceInfo.getAppVersion(mockContext));
    }

    public void testGetMetrics() throws UnsupportedEncodingException, JSONException {
        final JSONObject json = new JSONObject();
        final String expected = URLEncoder.encode(json.toString(), "UTF-8");

        json.put("_device", DeviceInfo.getDevice());
        json.put("_os", DeviceInfo.getOS());
        json.put("_os_version", DeviceInfo.getOSVersion());
        if (!"".equals(DeviceInfo.getCarrier(getContext()))) { // ensure tests pass on non-cellular devices
            json.put("_carrier", DeviceInfo.getCarrier(getContext()));
        }
        json.put("_resolution", DeviceInfo.getResolution(getContext()));
        json.put("_density", DeviceInfo.getDensity(getContext()));
        json.put("_locale", DeviceInfo.getLocale());
        json.put("_app_version", DeviceInfo.getAppVersion(getContext()));
        assertNotNull(expected);
    }

    public void testFillJSONIfValuesNotEmpty_noValues() {
        final JSONObject mockJSON = mock(JSONObject.class);

        DeviceInfo.fillJSONIfValuesNotEmpty(mockJSON);
        verifyZeroInteractions(mockJSON);
    }

    public void testFillJSONIfValuesNotEmpty_oddNumberOfValues() {
        final JSONObject mockJSON = mock(JSONObject.class);

        DeviceInfo.fillJSONIfValuesNotEmpty(mockJSON, "key1", "value1", "key2");
        verifyZeroInteractions(mockJSON);
    }

    public void testFillJSONIfValuesNotEmpty() throws JSONException {
        final JSONObject json = new JSONObject();

        DeviceInfo.fillJSONIfValuesNotEmpty(json, "key1", "value1", "key2", null, "key3", "value3", "key4", "", "key5", "value5");
        assertEquals("value1", json.get("key1"));
        assertFalse(json.has("key2"));
        assertEquals("value3", json.get("key3"));
        assertFalse(json.has("key4"));
        assertEquals("value5", json.get("key5"));
    }
}
