package com.habitrpg.android.habitica.modules;

import com.habitrpg.android.habitica.APIHelper;
import com.habitrpg.android.habitica.ContentCache;
import com.habitrpg.android.habitica.HostConfig;
import com.magicmicky.habitrpgwrapper.lib.api.ApiService;
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
        return APIHelper.createGsonFactory();
    }

    @Provides
    @Singleton
    public APIHelper providesApiHelper(GsonConverterFactory gsonConverter, HostConfig hostConfig) {
        return new APIHelper(gsonConverter, hostConfig);
    }

    @Provides
    @Singleton
    public ApiService providesApiService(APIHelper apiHelper) {
        return apiHelper.apiService;
    }


    @Provides
    @Singleton
    public ContentCache providesContentCache(APIHelper helper){
        return new ContentCache(helper);
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
