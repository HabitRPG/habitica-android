package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskCreationCallback implements Callback<Task> {
    @Override
    public void success(Task task, Response response) {
        task.save();
        EventBus.getDefault().post(new TaskCreatedEvent(task));
    }

    @Override
    public void failure(RetrofitError error) {
        Log.w("HabitCreation", "Error", error);
    }
}
