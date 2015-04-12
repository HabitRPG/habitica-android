package com.magicmicky.habitrpgmobileapp.callbacks;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskUpdateCallback implements Callback<HabitItem> {
    private OnHabitUpdated callback;

    public TaskUpdateCallback(OnHabitUpdated cb) {
        callback = cb;
    }
    @Override
    public void success(HabitItem habit, Response response) {
        callback.onTaskUpdated(habit);
    }

    @Override
    public void failure(RetrofitError error) {
        callback.onTaskUpdateFail();
        Log.w("HabitUpdate", "Error " + error.getMessage());
    }
    public interface OnHabitUpdated {
        void onTaskUpdated(HabitItem habit);
        void onTaskUpdateFail();
    }
}
