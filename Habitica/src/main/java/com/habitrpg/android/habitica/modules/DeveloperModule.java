package com.habitrpg.android.habitica.modules;


import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;
import com.habitrpg.android.habitica.proxy.implementation.EmptyCrashlyticsProxy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

//provide proxy class for libraries(to avoid 65k limit)
@Module
public class DeveloperModule {
    @Provides
    @Singleton
    protected CrashlyticsProxy provideCrashlyticsProxy() {
        return new EmptyCrashlyticsProxy();
    }

}
