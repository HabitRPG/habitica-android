package com.playseeds.android.sdk;

import android.test.AndroidTestCase;

import java.util.HashMap;

public class CrashDetailsTest extends AndroidTestCase {

    public void testAddLog() throws Exception {
        String record = "This is a new log";
        CrashDetails.addLog(record);
        String logs = CrashDetails.getLogs();

        assertEquals(record + "\n", logs);
        assertNotSame(record, logs);
    }

    public void testAddLog_WhenNullRecord() throws Exception {
        CrashDetails.addLog(null);
        String logs = CrashDetails.getLogs();

        assertNotSame(null, logs);
        assertNotNull(logs);
    }

    public void testSetCustomSegments() throws Exception {
        CrashDetails.setCustomSegments(new HashMap<String, String>());

        assertNull(CrashDetails.getCustomSegments());
    }

    public void testGetManufacturer() throws Exception {
        assertNotNull(CrashDetails.getManufacturer());
    }

    public void testGetCpu() throws Exception {
        assertNotNull(CrashDetails.getCpu());
    }

    public void testGetOpenGL() throws Exception {
        assertNotNull(CrashDetails.getOpenGL(getContext()));
    }

    public void testGetRamCurrent() throws Exception {
        assertNotNull(CrashDetails.getRamCurrent(getContext()));
    }

    public void testRamTotal() throws Exception {
        assertNotNull(CrashDetails.getRamTotal());
    }

    public void testGetDiskCurrent() throws Exception {
        assertNotNull(CrashDetails.getDiskCurrent());
    }

    public void testGetDiskTotal() throws Exception {
        assertNotNull(CrashDetails.getDiskTotal());
    }

    public void testGetBatteryLevel() throws Exception {
        assertNotNull(CrashDetails.getBatteryLevel(getContext()));
    }

    public void testGetRunningTime() throws Exception {
        assertNotNull(CrashDetails.getRunningTime());
    }

    public void testGetOrientation() throws Exception {
        assertNotNull(CrashDetails.getOrientation(getContext()));
    }

    public void testIsRooted() throws Exception {
        assertNotNull(CrashDetails.isRooted());
    }

    public void testIsOnline() throws Exception {
        assertNotNull(CrashDetails.isOnline(getContext()));
    }

    public void testIsMuted() throws Exception {
        assertNotNull(CrashDetails.isMuted(getContext()));
    }

    public void testGetCrashData() throws Exception {
        assertNotNull(CrashDetails.getCrashData(getContext(), "Error message", false));
    }
}
