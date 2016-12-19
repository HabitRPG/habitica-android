package com.playseeds.android.sdk.inappmessaging;


import android.test.AndroidTestCase;

public class RequestExceptionTest extends AndroidTestCase {

    public void testDefaultConstructor() throws Exception {
        try {
            throw new RequestException();
        } catch (RequestException e) {
            assertNull(e.getMessage());
            assertNull(e.getCause());
        }
    }

    public void testRequestException_WhenStringParameter() throws Exception {
        try {
            throw new RequestException("Request exception thrown");
        } catch(RequestException e) {
            assertNotNull(e.getMessage());
            assertNull(e.getCause());
        }
    }

    public void testRequestException_WhenStringAndThrowableParameter() throws Exception {
        try {
            throw new RequestException("Request exception thrown", new Throwable());
        } catch(RequestException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getCause());
        }
    }

    public void testRequestException_WhenThrowableParameter() throws Exception {
        try {
            throw new RequestException(new Throwable());
        } catch(RequestException e) {
            assertNotNull(e.getMessage());
            assertNotNull(e.getCause());
        }
    }
}
