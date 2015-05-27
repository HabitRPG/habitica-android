package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.HabitItem;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskDeletionCallback  implements Callback<Void> {
    private final OnTaskDeleted callback;
    private final HabitItem taskToDelete;

    public TaskDeletionCallback(OnTaskDeleted cb, HabitItem taskToDelete) {
        this.callback = cb;
        this.taskToDelete = taskToDelete;
    }
    @Override
    public void success(Void aVoid, Response response) {
        callback.onTaskDeleted(taskToDelete);
    }

    @Override
    public void failure(RetrofitError error) {
        Crashlytics.logException(error);

        callback.onTaskDeletionFail();
        Log.w("HabitDeletion", "Error " + error.getMessage());
    }


    public interface OnTaskDeleted {
        public void onTaskDeleted(HabitItem deleted);
        public void onTaskDeletionFail();
    }}
