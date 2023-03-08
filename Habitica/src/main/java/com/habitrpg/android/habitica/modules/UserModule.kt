package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
class UserModule {
    @Provides
    fun providesTaskAlarmManager(
        @ApplicationContext context: Context,
        taskRepository: TaskRepository,
        @Named(NAMED_USER_ID) userId: String
    ): TaskAlarmManager {
        return TaskAlarmManager(context, taskRepository, userId)
    }

    @Provides
    @Named(NAMED_USER_ID)
    fun providesUserID(sharedPreferences: SharedPreferences): String {
        return if (BuildConfig.DEBUG && BuildConfig.TEST_USER_ID.isNotEmpty()) {
            BuildConfig.TEST_USER_ID
        } else {
            sharedPreferences.getString("UserID", "") ?: ""
        }
    }

    @Provides
    fun providesUserViewModel(
        @Named(NAMED_USER_ID) userID: String,
        userRepository: UserRepository,
        socialRepository: SocialRepository
    ) = MainUserViewModel(userID, userRepository, socialRepository)

    companion object {
        const val NAMED_USER_ID = "userId"
    }
}
