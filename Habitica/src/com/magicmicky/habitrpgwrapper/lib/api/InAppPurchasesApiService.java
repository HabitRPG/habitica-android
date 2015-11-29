package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by Negue on 27.11.2015.
 */
public interface InAppPurchasesApiService {
    @POST("/iap/android/verify")
    PurchaseValidationResult validatePurchase(@Query("_id") String id, @Query("apiToken") String apiToken, @Body PurchaseValidationRequest request);
}
