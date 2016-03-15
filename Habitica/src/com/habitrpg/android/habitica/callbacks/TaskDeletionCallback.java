package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskDeletionCallback implements Callback<Void> {
    private final OnTaskDeleted callback;
    private final Task taskToDelete;

    public TaskDeletionCallback(OnTaskDeleted cb, Task taskToDelete) {
        this.callback = cb;
        this.taskToDelete = taskToDelete;
    }

    @Override
    public void success(Void aVoid, Response response) {
        callback.onTaskDeleted(taskToDelete);
    }

    @Override
    public void failure(RetrofitError error) {
        callback.onTaskDeletionFail();
        Log.w("HabitDeletion", "Error", error);
    }


    public interface OnTaskDeleted {
        public void onTaskDeleted(Task deleted);

        public void onTaskDeletionFail();
    }
}
