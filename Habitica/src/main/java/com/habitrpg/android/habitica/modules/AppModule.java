package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.HabiticaApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private HabiticaApplication application;

    public AppModule(HabiticaApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Context providesContext() {
        return application;
    }

    @Provides @Singleton
    public SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
