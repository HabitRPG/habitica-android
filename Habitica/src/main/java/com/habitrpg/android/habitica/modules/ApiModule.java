package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.helpers.PopupNotificationsManager;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.HostConfig;
import com.magicmicky.habitrpgwrapper.lib.api.MaintenanceApiService;

import android.content.Context;
import android.content.SharedPreferences;

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
        return com.habitrpg.android.habitica.ApiClientImpl.createGsonFactory();
    }

    @Provides
    @Singleton
    public PopupNotificationsManager providesPopupNotificationsManager(Context context) {
        return new PopupNotificationsManager(context);
    }

    @Provides
    @Singleton
    public ApiClient providesApiHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig, CrashlyticsProxy crashlyticsProxy, PopupNotificationsManager popupNotificationsManager, Context context) {
        return new com.habitrpg.android.habitica.ApiClientImpl(gsonConverter, hostConfig, crashlyticsProxy, popupNotificationsManager, context);
    }

    @Provides
    @Singleton
    public ContentCache providesContentCache(ApiClient client){
        return new ContentCache(client);
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
