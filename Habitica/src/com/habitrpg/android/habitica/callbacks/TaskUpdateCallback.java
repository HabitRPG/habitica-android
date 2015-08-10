package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskUpdateCallback implements Callback<Task> {
    private OnHabitUpdated callback;

    public TaskUpdateCallback(OnHabitUpdated cb) {
        callback = cb;
    }
    @Override
    public void success(Task habit, Response response) {
        callback.onTaskUpdated(habit);
    }

    @Override
    public void failure(RetrofitError error) {
        Crashlytics.logException(error);

        callback.onTaskUpdateFail();
        Log.w("HabitUpdate", "Error " + error.getMessage());
    }
    public interface OnHabitUpdated {
        void onTaskUpdated(Task habit);
        void onTaskUpdateFail();
    }
}
