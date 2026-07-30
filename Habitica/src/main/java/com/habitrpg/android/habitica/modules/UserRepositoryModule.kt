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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UserRepositoryModule {
    @Provides
    fun providesSetupCustomizationRepository(
        @ApplicationContext context: Context,
    ): SetupCustomizationRepository = SetupCustomizationRepositoryImpl(context)

    @Provides
    fun providesTaskLocalRepository(realm: Realm): TaskLocalRepository = RealmTaskLocalRepository(realm)

    @Provides
    fun providesTaskRepository(
        localRepository: TaskLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
        appConfigManager: AppConfigManager,
    ): TaskRepository =
        TaskRepositoryImpl(
            localRepository,
            apiClient,
            authenticationHandler,
            appConfigManager,
        )

    @Provides
    fun providesTagLocalRepository(realm: Realm): TagLocalRepository = RealmTagLocalRepository(realm)

    @Provides
    fun providesTagRepository(
        localRepository: TagLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): TagRepository = TagRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    fun provideChallengeLocalRepository(realm: Realm): ChallengeLocalRepository = RealmChallengeLocalRepository(realm)

    @Provides
    fun providesChallengeRepository(
        localRepository: ChallengeLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): ChallengeRepository = ChallengeRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    fun providesUserLocalRepository(realm: Realm): UserLocalRepository = RealmUserLocalRepository(realm)

    @Provides
    fun providesUserRepository(
        localRepository: UserLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
        taskRepository: TaskRepository,
        appConfigManager: AppConfigManager,
        @ApplicationContext context: Context,
    ): UserRepository =
        UserRepositoryImpl(
            localRepository,
            apiClient,
            authenticationHandler,
            taskRepository,
            appConfigManager,
            context,
        )

    @Provides
    fun providesSocialLocalRepository(realm: Realm): SocialLocalRepository = RealmSocialLocalRepository(realm)

    @Provides
    fun providesSocialRepository(
        localRepository: SocialLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): SocialRepository = SocialRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    fun providesInventoryLocalRepository(realm: Realm): InventoryLocalRepository = RealmInventoryLocalRepository(realm)

    @Provides
    fun providesInventoryRepository(
        localRepository: InventoryLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
        remoteConfig: AppConfigManager,
    ): InventoryRepository =
        InventoryRepositoryImpl(
            localRepository,
            apiClient,
            authenticationHandler,
            remoteConfig,
        )

    @Provides
    fun providesFAQLocalRepository(realm: Realm): FAQLocalRepository = RealmFAQLocalRepository(realm)

    @Provides
    fun providesFAQRepository(
        localRepository: FAQLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): FAQRepository = FAQRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    fun providesTutorialLocalRepository(realm: Realm): TutorialLocalRepository = RealmTutorialLocalRepository(realm)

    @Provides
    fun providesTutorialRepository(
        localRepository: TutorialLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): TutorialRepository = TutorialRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    fun providesCustomizationLocalRepository(realm: Realm): CustomizationLocalRepository = RealmCustomizationLocalRepository(realm)

    @Provides
    fun providesCustomizationRepository(
        localRepository: CustomizationLocalRepository,
        apiClient: ApiClient,
        authenticationHandler: AuthenticationHandler,
    ): CustomizationRepository = CustomizationRepositoryImpl(localRepository, apiClient, authenticationHandler)

    @Provides
    @Singleton
    fun providesPurchaseHandler(
        @ApplicationContext context: Context,
        apiClient: ApiClient,
        userViewModel: MainUserViewModel,
        appConfigManager: AppConfigManager,
    ): PurchaseHandler = PurchaseHandler(context, apiClient, userViewModel, appConfigManager)
}
