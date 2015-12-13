package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskUpdateCallback implements Callback<Task> {

    @Override
    public void success(Task task, Response response) {
        task.save();

        EventBus.getDefault().post(new TaskUpdatedEvent(task));
    }

    @Override
    public void failure(RetrofitError error) {
        Log.w("HabitUpdate", "Error " + error.getMessage());
    }

}
