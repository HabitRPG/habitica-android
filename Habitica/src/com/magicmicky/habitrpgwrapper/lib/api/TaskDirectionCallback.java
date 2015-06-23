package com.magicmicky.habitrpgwrapper.lib.api;

import android.util.Log;

import com.google.gson.Gson;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Simple implementation for TaskDirection callback. When the result is the result for a taskDirection.
 * Created by MagicMicky on 12/06/2014.
 */
public class TaskDirectionCallback implements Callback<TaskDirectionData> {
    private static final String TAG = "TaskDirection";
    @Override
    public void success(TaskDirectionData taskDirectionData, Response response) {
        Log.d(TAG, "Task value modified:" + taskDirectionData.getDelta());
        Log.d(TAG +"_ans", new Gson().toJson(taskDirectionData));

    }

    @Override
    public void failure(RetrofitError retrofitError) {
        Log.w(TAG, "failure!!");

    }
}
