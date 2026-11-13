package com.habitrpg.android.habitica.testing

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AnalyticsManager
import com.habitrpg.android.habitica.helpers.CrashReporter
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.PerformanceMonitor
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.RemoteConfig
import com.habitrpg.android.habitica.modules.ApiModule
import com.habitrpg.android.habitica.modules.PlatformServicesModule
import com.habitrpg.android.habitica.modules.RepositoryModule
import com.habitrpg.android.habitica.modules.UserRepositoryModule
import com.habitrpg.android.habitica.testing.fakes.FakeAnalyticsManager
import com.habitrpg.android.habitica.testing.fakes.FakeCrashReporter
import com.habitrpg.android.habitica.testing.fakes.FakeNavigator
import com.habitrpg.android.habitica.testing.fakes.FakeRemoteConfig
import com.habitrpg.android.habitica.testing.fakes.NoOpPerformanceMonitor
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.helpers.Navigator
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PlatformServicesModule::class])
object TestPlatformServicesModule {
    @Provides
    @Singleton
    fun provideFakeNavigator(): FakeNavigator = FakeNavigator()

    @Provides
    fun provideNavigator(navigator: FakeNavigator): Navigator = navigator

    @Provides
    @Singleton
    fun provideFakeAnalyticsManager(): FakeAnalyticsManager = FakeAnalyticsManager()

    @Provides
    fun provideAnalyticsManager(analytics: FakeAnalyticsManager): AnalyticsManager = analytics

    @Provides
    @Singleton
    fun provideFakeCrashReporter(): FakeCrashReporter = FakeCrashReporter()

    @Provides
    fun provideCrashReporter(crashReporter: FakeCrashReporter): CrashReporter = crashReporter

    @Provides
    @Singleton
    fun provideFakeRemoteConfig(): FakeRemoteConfig = FakeRemoteConfig()

    @Provides
    fun provideRemoteConfig(remoteConfig: FakeRemoteConfig): RemoteConfig = remoteConfig

    @Provides
    fun providePerformanceMonitor(): PerformanceMonitor = NoOpPerformanceMonitor()
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class, UserRepositoryModule::class],
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideContentRepository(): ContentRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideTaskRepository(): TaskRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideTagRepository(): TagRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideChallengeRepository(): ChallengeRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSocialRepository(): SocialRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideInventoryRepository(): InventoryRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideFAQRepository(): FAQRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideTutorialRepository(): TutorialRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideCustomizationRepository(): CustomizationRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSetupCustomizationRepository(): SetupCustomizationRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun providePurchaseHandler(): PurchaseHandler = mockk(relaxed = true)
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ApiModule::class])
object TestApiModule {
    @Provides
    @Singleton
    fun provideApiClient(): ApiClient = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideHostConfig(): HostConfig = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideNotificationsManager(): NotificationsManager = mockk(relaxed = true)

    @Provides
    fun provideMaintenanceApiService(): MaintenanceApiService = mockk(relaxed = true)

    @Provides
    fun provideGsonConverterFactory(performanceMonitor: PerformanceMonitor): GsonConverterFactory =
        GSonFactoryCreator.create(performanceMonitor)
}
