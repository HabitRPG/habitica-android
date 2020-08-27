package com.habitrpg.android.habitica;


import com.habitrpg.android.habitica.modules.DeveloperModule;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxyImpl;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import android.content.Context;

//change debug proxy here by override methods
public class ReleaseDeveloperModule extends DeveloperModule {
    @Override protected CrashlyticsProxy provideCrashlyticsProxy(Context context) {
        return new CrashlyticsProxyImpl();
    }
}
