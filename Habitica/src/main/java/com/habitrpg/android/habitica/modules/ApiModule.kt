package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import com.habitrpg.android.habitica.api.HabiticaConverterFactory
import com.habitrpg.android.habitica.api.MaintenanceApiService
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.implementation.ApiClientImpl
import com.habitrpg.android.habitica.helpers.AnalyticsManager
import com.habitrpg.android.habitica.helpers.MainNotificationsManager
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.PerformanceMonitor
import com.habitrpg.common.habitica.api.HostConfig
import com.habitrpg.common.habitica.helpers.KeyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.ref.WeakReference
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
open class ApiModule {
    @Provides
    @Singleton
    fun providesHostConfig(
        sharedPreferences: SharedPreferences,
        keyHelper: KeyHelper?,
        @ApplicationContext context: Context,
    ): HostConfig = HostConfig(sharedPreferences, keyHelper, context)

    @Provides
    fun providesConverterFactory(performanceMonitor: PerformanceMonitor): Converter.Factory = HabiticaConverterFactory(performanceMonitor)

    @Provides
    @Singleton
    fun providesPopupNotificationsManager(): NotificationsManager = MainNotificationsManager()

    @Provides
    @Singleton
    fun providesApiHelper(
        converterFactory: Converter.Factory,
        hostConfig: HostConfig,
        notificationsManager: NotificationsManager,
        @ApplicationContext context: Context,
        analytics: AnalyticsManager,
    ): ApiClient {
        val apiClient =
            ApiClientImpl(
                converterFactory,
                hostConfig,
                notificationsManager,
                context,
                analytics,
            )
        notificationsManager.apiClient = WeakReference(apiClient)
        return apiClient
    }

    @Provides
    fun providesMaintenanceApiService(converterFactory: Converter.Factory): MaintenanceApiService {
        val adapter =
            Retrofit
                .Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addConverterFactory(converterFactory)
                .build()
        return adapter.create(MaintenanceApiService::class.java)
    }
}
