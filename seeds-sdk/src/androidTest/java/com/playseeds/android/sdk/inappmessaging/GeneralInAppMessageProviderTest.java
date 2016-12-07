package com.playseeds.android.sdk.inappmessaging;

import android.test.AndroidTestCase;

public class GeneralInAppMessageProviderTest extends AndroidTestCase {
    GeneralInAppMessageProvider generalInAppMessageProvider;

    public void setUp() throws Exception {
        generalInAppMessageProvider = new GeneralInAppMessageProvider();
    }

    public void testParseCountlyJsonWithNullValues() throws Exception {
        try {
            generalInAppMessageProvider.parseCountlyJSON(null, null);
            fail("RequestException expected");
        } catch (RequestException e) {
            // success
        }
    }
}
