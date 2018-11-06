package com.habitrpg.android.habitica.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.executors.JobExecutor;
import com.habitrpg.android.habitica.executors.PostExecutionThread;
import com.habitrpg.android.habitica.executors.ThreadExecutor;
import com.habitrpg.android.habitica.executors.UIThread;
import com.habitrpg.android.habitica.helpers.RemoteConfigManager;
import com.habitrpg.android.habitica.helpers.SoundFileLoader;
import com.habitrpg.android.habitica.helpers.SoundManager;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    public static final String NAMED_USER_ID = "userId";


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
    @Named(NAMED_USER_ID)
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
    TaskAlarmManager providesTaskAlarmManager(Context context, TaskRepository taskRepository, @Named(NAMED_USER_ID) String userId) {
        return new TaskAlarmManager(context, taskRepository, userId);
    }

    @Provides
    @Singleton
    PushNotificationManager pushNotificationManager(ApiClient apiClient, SharedPreferences sharedPreferences, Context context) {
        return new PushNotificationManager(apiClient, sharedPreferences, context);
    }

    @Provides
    @Singleton
    RemoteConfigManager providesRemoteConfiigManager(Context context) {
        return new RemoteConfigManager(context);
    }
}
