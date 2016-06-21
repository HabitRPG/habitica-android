package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.HabiticaApplication;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.helpers.TagsHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Named;
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

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @Named("UserID")
    public String providesUserID(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(application.getString(R.string.SP_userID), null);
    }

    @Provides
    @Singleton
    public TagsHelper providesTagsHelper() {
        return new TagsHelper();
    }
}
