package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;

public class LogTest extends AndroidTestCase {

    public void testIsLoggable() throws Exception {
        assertTrue(Log.isLoggable(0));
    }

    public void testDebug() {
        Log.d("random message");
    }

    public void testDebugThrowable() throws Exception {
        Log.d("faker", new Throwable());
    }

    public void testError() throws Exception {
        Log.e("error");
    }

    public void testErrorThrowable() throws Exception {
        Log.e("error", new Throwable());
    }

    public void testInfo() throws Exception {
        Log.i("Info");
    }

    public void testInfoThrowable() throws Exception {
        Log.i("info", new Throwable());
    }

    public void testVerbose() throws Exception {
        Log.v("verbose");
    }

    public void testVerboseThrowable() throws Exception {
        Log.v("verbose", new Throwable());
    }

    public void testW() throws Exception {
        Log.w("Www");
    }

    public void testWThrowable() throws Exception {
        Log.w("www", new Throwable());
    }
}
