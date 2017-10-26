package com.habitrpg.android.habitica.api;

import com.habitrpg.android.habitica.models.responses.HabitResponse;
import com.habitrpg.android.habitica.models.responses.MaintenanceResponse;

import retrofit2.http.GET;
import rx.Observable;

public interface MaintenanceApiService {

    @GET("maintenance-android.json")
    Observable<HabitResponse<MaintenanceResponse>> getMaintenanceStatus();

    @GET("deprecation-android.json")
    Observable<HabitResponse<MaintenanceResponse>> getDepricationStatus();

}
