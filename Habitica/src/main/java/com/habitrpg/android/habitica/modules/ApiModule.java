package com.habitrpg.android.habitica.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.habitrpg.android.habitica.api.HostConfig;
import com.habitrpg.android.habitica.api.MaintenanceApiService;
import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.implementation.ApiClientImpl;
import com.habitrpg.android.habitica.helpers.PopupNotificationsManager;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiModule {

    @Provides
    @Singleton
    public HostConfig providesHostConfig(SharedPreferences sharedPreferences, Context context) {
        return new HostConfig(sharedPreferences, context);
    }

    @Provides
    public GsonConverterFactory providesGsonConverterFactory() {
        return ApiClientImpl.createGsonFactory();
    }

    @Provides
    @Singleton
    public PopupNotificationsManager providesPopupNotificationsManager(Context context) {
        return new PopupNotificationsManager(context);
    }

    @Provides
    @Singleton
    public ApiClient providesApiHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig, CrashlyticsProxy crashlyticsProxy, PopupNotificationsManager popupNotificationsManager, Context context) {
        return new ApiClientImpl(gsonConverter, hostConfig, crashlyticsProxy, popupNotificationsManager, context);
    }

    @Provides
    public MaintenanceApiService providesMaintenanceApiService(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        Retrofit adapter = new Retrofit.Builder()
                .baseUrl("https://habitica-assets.s3.amazonaws.com/mobileApp/endpoint/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(gsonConverter)
                .build();
        return adapter.create(MaintenanceApiService.class);
    }
}
