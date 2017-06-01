package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.executors.JobExecutor;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.executors.UIThread;
import com.habitrpg.android.habitica.helpers.SoundFileLoader;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;

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
        return sharedPreferences.getString(application.getString(R.string.SP_userID), "");
    }

    @Provides
    @Singleton
    public TaskFilterHelper providesTagsHelper() {
        return new TaskFilterHelper();
    }

    @Provides
    public Resources providesResources(Context context) {
        return context.getResources();
    }

    @Provides
    public SoundFileLoader providesSoundFileLoader(Context context) {
        return new SoundFileLoader(context);
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

    @Provides
    @Singleton
    TaskAlarmManager providesTaskAlarmManager(Context context) {
        return new TaskAlarmManager(context);
    }

    @Provides
    @Singleton
    PushNotificationManager pushNotificationManager(ApiClient apiClient, SharedPreferences sharedPreferences, Context context) {
        return new PushNotificationManager(apiClient, sharedPreferences, context);
    }
}
