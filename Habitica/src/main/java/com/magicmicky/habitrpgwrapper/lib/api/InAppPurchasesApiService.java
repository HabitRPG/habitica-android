package com.magicmicky.habitrpgwrapper.lib.api;

import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationRequest;
import com.magicmicky.habitrpgwrapper.lib.models.PurchaseValidationResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;


/**
 * Created by Negue on 27.11.2015.
 */
public interface InAppPurchasesApiService {
    @POST("/iap/android/verify")
    Call<PurchaseValidationResult> validatePurchase(@Query("_id") String id, @Query("apiToken") String apiToken, @Body PurchaseValidationRequest request);
}
