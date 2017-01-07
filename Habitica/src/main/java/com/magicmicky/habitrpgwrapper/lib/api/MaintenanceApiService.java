package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.responses.MaintenanceResponse;

import retrofit2.http.GET;
import rx.Observable;

public interface MaintenanceApiService {

    @GET("maintenance-android.json")
    Observable<HabitResponse<MaintenanceResponse>> getMaintenanceStatus();

    @GET("deprecation-android.json")
    Observable<HabitResponse<MaintenanceResponse>> getDepricationStatus();

}
