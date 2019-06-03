package com.habitrpg.android.habitica.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.UserScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class UserModule {
    public static final String NAMED_USER_ID = "userId";

    @Provides
    @UserScope
    TaskAlarmManager providesTaskAlarmManager(Context context, TaskRepository taskRepository, @Named(NAMED_USER_ID) String userId) {
        return new TaskAlarmManager(context, taskRepository, userId);
    }

    @Provides
    @Named(NAMED_USER_ID)
    @UserScope
    public String providesUserID(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("UserID", "");
    }
}
