package com.habitrpg.android.habitica.modules

import android.content.Context
import android.content.SharedPreferences
import com.habitrpg.android.habitica.apiService.MaintenanceApiService
import com.habitrpg.android.habitica.apiclient.ApiClient
import com.habitrpg.android.habitica.apiclient.ApiClientImpl
import com.habitrpg.android.habitica.apiclient.ApiClientImpl.Companion.createGsonFactory
import com.habitrpg.android.habitica.apiclient.ConnectionProblemDialogs
import com.habitrpg.android.habitica.apiclient.OkhttpWrapper
import com.habitrpg.android.habitica.apiclient.MainNotificationsManager
import com.habitrpg.android.habitica.apiclient.NotificationsManager
import com.habitrpg.android.habitica.apiService.HostConfig
import com.habitrpg.common.habitica.helpers.KeyHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    ): HostConfig {
        return HostConfig(sharedPreferences, keyHelper, context)
    }

    @Provides
    fun providesGsonConverterFactory(): GsonConverterFactory {
        return createGsonFactory()
    }

    @Provides
    @Singleton
    fun providesPopupNotificationsManager(): NotificationsManager {
        return MainNotificationsManager()
    }

    @Provides
    @Singleton
    fun providesApiHelper(
        gsonConverter: GsonConverterFactory,
        hostConfig: HostConfig,
        notificationsManager: NotificationsManager,
        dialogs: ConnectionProblemDialogs,
        okhttpWrapper: OkhttpWrapper
    ): ApiClient {
        val apiClient =
            ApiClientImpl(
                gsonConverter,
                hostConfig,
                okhttpWrapper,
                notificationsManager,
                dialogs
            )
        notificationsManager.apiClient = WeakReference(apiClient)
        return apiClient
    }

    @Provides
    fun providesMaintenanceApiService(gsonConverter: GsonConverterFactory): MaintenanceApiService {
        val adapter =
            Retrofit.Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addConverterFactory(gsonConverter)
                .build()
        return adapter.create(MaintenanceApiService::class.java)
    }
}
