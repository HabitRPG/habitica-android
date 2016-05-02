package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.responses.MaintenanceResponse;

import retrofit.Callback;
import retrofit.http.GET;

public interface MaintenanceApiService {

    @GET("/maintenance-android.json")
    void getMaintenanceStatus(Callback<MaintenanceResponse> statusCallback);

    @GET("/deprecation-android.json")
    void getDepricationStatus(Callback<MaintenanceResponse> statusCallback);

}
