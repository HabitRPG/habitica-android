package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.Status;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple Status callback implementation.
 * Created by MagicMicky on 10/06/2014.
 */
public class StatusCallback implements Callback<Status> {
    private static final String TAG = "Status";

    @Override
    public void success(Status status, Response response) {
        Log.d(TAG, "Success ! " + status.getStatus());
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());

    }
}
