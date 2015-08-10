package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * TODO: Tags
 * Simple implementation of a Task callback, supposed to be called when the result is either a single
 * to do, daily, habit or reward.
 * Created by MagicMicky on 10/06/2014.
 */
public class HabitItemCallback<T extends Task> implements Callback<T> {
    private static final String TAG = "HabitCallback";

    @Override
    public void success(T habitItem, Response response) {


    }

    @Override
    public void failure(RetrofitError retrofitError){
        Log.w(TAG, "Failure ! ");
        Log.e(TAG, retrofitError.getUrl() + ":" + retrofitError.getMessage());
        Log.e(TAG, "Network?" + retrofitError.isNetworkError());
        Log.e(TAG, "HTTP Response:" + retrofitError.getResponse().getStatus() + " - " + retrofitError.getResponse().getReason());
    }
}
