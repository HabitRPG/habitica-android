package com.habitrpg.android.habitica.events;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

/**
 * Created by Negue on 28.09.2015.
 */
public class TaskCreatedEvent {
    public Task task;

    public TaskCreatedEvent(Task t){
        task = t;
    }
}
