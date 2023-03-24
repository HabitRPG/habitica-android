package com.habitrpg.android.habitica.modules

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.data.implementation.ChallengeRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.CustomizationRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.FAQRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.InventoryRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.SetupCustomizationRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.SocialRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.TagRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.TaskRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.TutorialRepositoryImpl
import com.habitrpg.android.habitica.data.implementation.UserRepositoryImpl
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository
import com.habitrpg.android.habitica.data.local.FAQLocalRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.data.local.SocialLocalRepository
import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.data.local.TaskLocalRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.data.local.UserLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmChallengeLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmCustomizationLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmFAQLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmInventoryLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmSocialLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmTagLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmTaskLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmTutorialLocalRepository
import com.habitrpg.android.habitica.data.local.implementation.RealmUserLocalRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import io.realm.Realm
import javax.inject.Named

@InstallIn(ActivityRetainedComponent::class)
@Module
class UserRepositoryModule {
    @Provides
    fun providesSetupCustomizationRepository(@ApplicationContext context: Context?): SetupCustomizationRepository {
        return SetupCustomizationRepositoryImpl(context!!)
    }

    @Provides
    fun providesTaskLocalRepository(realm: Realm?): TaskLocalRepository {
        return RealmTaskLocalRepository(realm!!)
    }

    @Provides
    fun providesTaskRepository(
        localRepository: TaskLocalRepository,
        apiClient: ApiClient,
        @Named(AppModule.NAMED_USER_ID) userId: String,
        appConfigManager: AppConfigManager,
        analyticsManager: AnalyticsManager
    ): TaskRepository {
        return TaskRepositoryImpl(
            localRepository,
            apiClient,
            userId,
            appConfigManager,
            analyticsManager
        )
    }

    @Provides
    fun providesTagLocalRepository(realm: Realm?): TagLocalRepository {
        return RealmTagLocalRepository(realm!!)
    }

    @Provides
    fun providesTagRepository(
        localRepository: TagLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): TagRepository {
        return TagRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    fun provideChallengeLocalRepository(realm: Realm?): ChallengeLocalRepository {
        return RealmChallengeLocalRepository(realm!!)
    }

    @Provides
    fun providesChallengeRepository(
        localRepository: ChallengeLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): ChallengeRepository {
        return ChallengeRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    fun providesUserLocalRepository(realm: Realm?): UserLocalRepository {
        return RealmUserLocalRepository(realm!!)
    }

    @Provides
    fun providesUserRepository(
        localRepository: UserLocalRepository,
        apiClient: ApiClient,
        @Named(AppModule.NAMED_USER_ID) userId: String,
        taskRepository: TaskRepository,
        appConfigManager: AppConfigManager,
        analyticsManager: AnalyticsManager
    ): UserRepository {
        return UserRepositoryImpl(
            localRepository,
            apiClient,
            userId,
            taskRepository,
            appConfigManager,
            analyticsManager
        )
    }

    @Provides
    fun providesSocialLocalRepository(realm: Realm?): SocialLocalRepository {
        return RealmSocialLocalRepository(realm!!)
    }

    @Provides
    fun providesSocialRepository(
        localRepository: SocialLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): SocialRepository {
        return SocialRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    fun providesInventoryLocalRepository(
        realm: Realm?): InventoryLocalRepository {
        return RealmInventoryLocalRepository(realm!!)
    }

    @Provides
    fun providesInventoryRepository(
        localRepository: InventoryLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?,
        remoteConfig: AppConfigManager?
    ): InventoryRepository {
        return InventoryRepositoryImpl(localRepository!!, apiClient!!, userId!!, remoteConfig!!)
    }

    @Provides
    fun providesFAQLocalRepository(realm: Realm?): FAQLocalRepository {
        return RealmFAQLocalRepository(realm!!)
    }

    @Provides
    fun providesFAQRepository(
        localRepository: FAQLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): FAQRepository {
        return FAQRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    fun providesTutorialLocalRepository(realm: Realm?): TutorialLocalRepository {
        return RealmTutorialLocalRepository(realm!!)
    }

    @Provides
    fun providesTutorialRepository(
        localRepository: TutorialLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): TutorialRepository {
        return TutorialRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    fun providesCustomizationLocalRepository(realm: Realm?): CustomizationLocalRepository {
        return RealmCustomizationLocalRepository(realm!!)
    }

    @Provides
    fun providesCustomizationRepository(
        localRepository: CustomizationLocalRepository?,
        apiClient: ApiClient?,
        @Named(AppModule.NAMED_USER_ID) userId: String?
    ): CustomizationRepository {
        return CustomizationRepositoryImpl(localRepository!!, apiClient!!, userId!!)
    }

    @Provides
    @ActivityScoped
    fun providesPurchaseHandler(
        @ApplicationContext context: Context,
        analyticsManager: AnalyticsManager,
        apiClient: ApiClient,
        userViewModel: MainUserViewModel
    ): PurchaseHandler {
        return PurchaseHandler(context, analyticsManager, apiClient, userViewModel)
    }
}
