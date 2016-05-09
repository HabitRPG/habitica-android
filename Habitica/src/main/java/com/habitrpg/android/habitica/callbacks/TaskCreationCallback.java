package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import rx.functions.Action1;

/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskCreationCallback implements Action1<Task> {

    @Override
    public void call(Task task) {
        task.save();
        EventBus.getDefault().post(new TaskCreatedEvent(task));
    }
}
