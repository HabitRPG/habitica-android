package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple void callback implementation.
 * Created by MagicMicky on 12/06/2014.
 */
public class VoidCallback implements Callback<Void> {
    private static final String TAG = "VOID";

    @Override
    public void success(Void aVoid, Response response) {
        Log.d(TAG, "Success with void!");
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());

    }
}
