package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple tag callback implementation. Used when the result is only a single tag.
 * Created by MagicMicky on 12/06/2014.
 */
public class TagCallback implements Callback<Tag> {
    private static final String TAG = "SingleTag";

    @Override
    public void success(Tag tag, Response response) {

        Log.d(TAG, tag.getName());
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());

    }
}
