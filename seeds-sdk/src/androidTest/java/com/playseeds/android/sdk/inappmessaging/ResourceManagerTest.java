package com.playseeds.android.sdk.inappmessaging;

import android.content.Context;
import android.test.AndroidTestCase;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ResourceManagerTest  extends AndroidTestCase {
    ResourceManager resourceManager;
    Context context;

    public void setUp() throws Exception {
        resourceManager = new ResourceManager();
        context = getContext();
    }

    public void testReleaseInstance() throws Exception {
        ResourceManager spyResourceManager = spy(resourceManager);

        doCallRealMethod().when(spyResourceManager).releaseInstance();
        spyResourceManager.releaseInstance();

        verify(spyResourceManager).releaseInstance();
    }

    public void testIsDownloading() throws Exception {
        assertFalse(ResourceManager.isDownloading());
    }

    public void testCancel() throws Exception {
        ResourceManager.cancel();
        assertTrue(resourceManager.getsResources().isEmpty());
    }

    public void testGetResource_WhenFakeResourceId() throws Exception {
        assertNull(resourceManager.getResource(context, android.R.layout.simple_list_item_1));
    }

    public void testGetResource_WithDefaultCloseButtonId() throws Exception {
        assertNotNull(resourceManager.getResource(context, -29));
    }

    public void testGetStaticResource_WhenFakeResourceId() throws Exception {
        int id = android.R.layout.simple_list_item_1;
        assertNull(ResourceManager.getStaticResource(context, id));
    }

    public void testGetStaticResource() throws Exception {
        assertNotNull(ResourceManager.getStaticResource(context, -11));
    }

    public void testGetStaticResource_WithDefaultCloseButtonId() throws Exception {
        assertNotNull(ResourceManager.getStaticResource(context, -29));
    }
}
