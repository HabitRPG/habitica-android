package com.habitrpg.android.habitica.modules

import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.common.habitica.helpers.Clearable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@InstallIn(SingletonComponent::class)
@Module
abstract class ClearableModule {
    @Binds
    @IntoSet
    abstract fun bindSoundManager(manager: SoundManager): Clearable

    @Binds
    @IntoSet
    abstract fun bindAppConfigManager(manager: AppConfigManager): Clearable

    @Binds
    @IntoSet
    abstract fun bindPushNotificationManager(manager: PushNotificationManager): Clearable

    @Binds
    @IntoSet
    abstract fun bindNotificationsManager(manager: NotificationsManager): Clearable

    @Binds
    @IntoSet
    abstract fun bindTaskAlarmManager(manager: TaskAlarmManager): Clearable

    @Binds
    @IntoSet
    abstract fun bindPurchaseHandler(handler: PurchaseHandler): Clearable

    @Binds
    @IntoSet
    abstract fun bindUserRepository(repository: UserRepository): Clearable

    @Binds
    @IntoSet
    abstract fun bindTaskRepository(repository: TaskRepository): Clearable

    @Binds
    @IntoSet
    abstract fun bindSocialRepository(repository: SocialRepository): Clearable
}
