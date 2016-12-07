package com.playseeds.android.sdk;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;

public class SeedsTestsRunner extends RobolectricGradleTestRunner {

    public SeedsTestsRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String manifestProperty = System.getProperty("android.manifest");
        if (config.manifest().equals(Config.DEFAULT) && manifestProperty != null) {
            String resProperty = System.getProperty("android.resources");
            String assetsProperty = System.getProperty("android.assets");
            AndroidManifest androidManifest = new AndroidManifest(
                    Fs.fileFromPath(manifestProperty),
                    Fs.fileFromPath(resProperty),
                    Fs.fileFromPath(assetsProperty));
            androidManifest.setPackageName("com.testPackage");
            return androidManifest;
        }
        return super.getAppManifest(config);
    }


}
