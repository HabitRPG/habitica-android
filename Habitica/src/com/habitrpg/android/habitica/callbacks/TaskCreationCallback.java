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
public class TaskCreationCallback implements Callback<Task> {
    private OnHabitCreated callback;

    public TaskCreationCallback(OnHabitCreated cb) {
        callback = cb;
    }



    @Override
    public void success(Task habit, Response response) {
        callback.onTaskCreated(habit);
    }

    @Override
    public void failure(RetrofitError error) {
        Crashlytics.logException(error);

        callback.onTaskCreationFail();
        Log.w("HabitCreation", "Error " + error.getMessage());
    }

    public interface OnHabitCreated {
        public void onTaskCreated(Task habit);
        public void onTaskCreationFail();
    }
}
