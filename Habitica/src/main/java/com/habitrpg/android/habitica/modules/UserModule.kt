package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.UserScope
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class UserModule {
    @Provides
    @UserScope
    fun providesTaskAlarmManager(
        context: Context,
        taskRepository: TaskRepository,
        @Named(NAMED_USER_ID) userId: String
    ): TaskAlarmManager {
        return TaskAlarmManager(context, taskRepository, userId)
    }

    @Provides
    @Named(NAMED_USER_ID)
    @UserScope
    fun providesUserID(sharedPreferences: SharedPreferences): String {
        return if (BuildConfig.DEBUG && BuildConfig.TEST_USER_ID.isNotEmpty()) {
            BuildConfig.TEST_USER_ID
        } else {
            sharedPreferences.getString("UserID", "") ?: ""
        }
    }

    @Provides
    @UserScope
    fun providesUserViewModel(
        @Named(NAMED_USER_ID) userID: String,
        userRepository: UserRepository,
        socialRepository: SocialRepository
    ) = MainUserViewModel(userID, userRepository, socialRepository)

    companion object {
        const val NAMED_USER_ID = "userId"
    }
}