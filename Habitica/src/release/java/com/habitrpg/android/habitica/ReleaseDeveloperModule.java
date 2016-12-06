package com.habitrpg.android.habitica;


import com.habitrpg.android.habitica.modules.DeveloperModule;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxyImpl;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;

//change debug proxy here by override methods
public class ReleaseDeveloperModule extends DeveloperModule {
    @Override protected CrashlyticsProxy provideCrashlyticsProxy() {
        return new CrashlyticsProxyImpl();
    }
}
