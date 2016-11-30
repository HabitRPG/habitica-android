package com.playseeds.android.sdk;

import android.test.AndroidTestCase;

public class DeviceIdTests extends AndroidTestCase {
    DeviceId deviceId;
    DeviceId.Type deviceType;
    boolean exceptionThrown;
    String id;

    protected void setUp() throws Exception {
        exceptionThrown = false;
        id = "Nexus-XLR";
        deviceType = null;
    }

    public void testDeviceIdConstructor_WhenNullParameter() throws Exception {
        try {
            deviceId = new DeviceId(deviceType);
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    public void testDeviceIdConstructor_WhenTypeIsDeveloperSupplied() throws Exception {
        deviceType = DeviceId.Type.DEVELOPER_SUPPLIED;

        try {
            deviceId = new DeviceId(deviceType);
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    public void testDeviceIdConstructor_WhenDeveloperSuppliedIsEmpty() throws Exception {
        id = "";

        try {
            deviceId = new DeviceId(id);
        } catch (IllegalStateException e) {
            exceptionThrown= true;
        }

        assertTrue(exceptionThrown);
    }

    public void testDeviceIdConstructor_WhenDeveloperSupplied() throws Exception {
        try {
            deviceId = new DeviceId(id);
        } catch(IllegalStateException e) {
            exceptionThrown = true;
        }

        assertFalse(exceptionThrown);
    }

    public void testDeviceIdEqualsNullSafe() throws Exception {
        deviceType = DeviceId.Type.ADVERTISING_ID;

        assertTrue(DeviceId.deviceIDEqualsNullSafe(id, deviceType, new DeviceId(deviceType)));
    }

    public void testDeviceIdEqualsNullSafe_WhenIdIsNull() throws Exception {
        deviceType = DeviceId.Type.ADVERTISING_ID;

        assertTrue(DeviceId.deviceIDEqualsNullSafe(null, deviceType, new DeviceId(deviceType)));
    }

    public void testDeviceIdEqualsNullSafe_WhenTypeIsNull() throws Exception {
        assertFalse(DeviceId.deviceIDEqualsNullSafe(id, deviceType, new DeviceId(DeviceId.Type.OPEN_UDID)));
    }
}
