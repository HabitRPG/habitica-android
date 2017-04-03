package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.executors.JobExecutor;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.executors.UIThread;
import com.habitrpg.android.habitica.helpers.SoundFileLoader;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TagsHelper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.preference.PreferenceManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
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

    @Provides
    public Resources providesResources(Context context) {
        return context.getResources();
    }

    @Provides
    public SoundFileLoader providesSoundFileLoader() {
        return new SoundFileLoader();
    }

    @Provides
    @Singleton
    public SoundManager providesSoundManager() {
        return new SoundManager();
    }


    @Provides
    @Singleton
    ThreadExecutor provideThreadExecutor(JobExecutor jobExecutor) {
        return jobExecutor;
    }


    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }
}
