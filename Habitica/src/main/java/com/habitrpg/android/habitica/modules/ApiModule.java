package com.habitrpg.android.habitica.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.habitrpg.android.habitica.api.HostConfig;
import com.habitrpg.android.habitica.api.MaintenanceApiService;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.implementation.ApiClientImpl;
import com.habitrpg.android.habitica.helpers.NotificationsManager;
import com.habitrpg.android.habitica.helpers.KeyHelper;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiModule {

    @Provides
    @Singleton
    public HostConfig providesHostConfig(SharedPreferences sharedPreferences, @Nullable KeyHelper keyHelper, Context context) {
        return new HostConfig(sharedPreferences, keyHelper, context);
    }

    @Provides
    public GsonConverterFactory providesGsonConverterFactory() {
        return ApiClientImpl.Companion.createGsonFactory();
    }

    @Provides
    @Singleton
    public NotificationsManager providesPopupNotificationsManager(Context context) {
        return new NotificationsManager(context);
    }

    @Provides
    @Singleton
    public ApiClient providesApiHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig, CrashlyticsProxy crashlyticsProxy, NotificationsManager notificationsManager, Context context) {
        return new ApiClientImpl(gsonConverter, hostConfig, crashlyticsProxy, notificationsManager, context);
    }

    @Provides
    public MaintenanceApiService providesMaintenanceApiService(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        Retrofit adapter = new Retrofit.Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        return adapter.create(MaintenanceApiService.class);
    }
}
