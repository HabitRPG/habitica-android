package com.magicmicky.habitrpgmobileapp.callbacks;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 18/02/15.
 */
public class TaskScoringCallback implements Callback<TaskDirectionData> {
    private final OnTaskScored mCallback;

    public TaskScoringCallback(OnTaskScored callback) {
        this.mCallback= callback;

    }
    @Override
    public void success(TaskDirectionData taskDirectionData, Response response) {
        this.mCallback.onTaskDataReceived(taskDirectionData);
    }

    @Override
    public void failure(RetrofitError error) {
        this.mCallback.onTaskScoringFailed();
        Log.w("TaskScoring", "Task scoring failed " + error.getMessage());
    }

    public interface OnTaskScored {
        public void onTaskDataReceived(TaskDirectionData data);
        public void onTaskScoringFailed();
    }
}
