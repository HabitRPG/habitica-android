package com.habitrpg.android.habitica.api;

import com.habitrpg.common.habitica.models.responses.MaintenanceResponse;

import io.reactivex.rxjava3.core.Flowable;
import retrofit2.http.GET;

public interface MaintenanceApiService {

    @GET("maintenance-android.json")
    Flowable<MaintenanceResponse> getMaintenanceStatus();

    @GET("deprecation-android.json")
    Flowable<MaintenanceResponse> getDepricationStatus();

}
