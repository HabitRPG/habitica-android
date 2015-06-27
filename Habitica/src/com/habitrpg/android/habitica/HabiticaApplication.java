package com.habitrpg.android.habitica;

import android.app.Application;

import com.instabug.library.Instabug;

/**
 * Created by Negue on 14.06.2015.
 */
public class HabiticaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Instabug.DEBUG = BuildConfig.DEBUG;
        Instabug.initialize(this, "a5aa5f471a9cd8a958c0c55181172655");
    }
}
