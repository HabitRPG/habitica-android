package com.habitrpg.android.habitica.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.UserRepository;
import com.habitrpg.android.habitica.helpers.TaskAlarmManager;
import com.habitrpg.android.habitica.helpers.UserScope;
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel;

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
        if (BuildConfig.DEBUG && !BuildConfig.TEST_USER_ID.isEmpty()) {
            return BuildConfig.TEST_USER_ID;
        } else {
            return sharedPreferences.getString("UserID", "");
        }
    }

    @Provides
    @UserScope
    MainUserViewModel providesUserViewModel(String userID, UserRepository userRepository) {
        return new MainUserViewModel(userID, userRepository);
    }
}
