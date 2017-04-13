package com.habitrpg.android.habitica.events;

import com.habitrpg.android.habitica.models.tasks.Task;

/**
 * Created by Negue on 28.09.2015.
 */
public class TaskCreatedEvent {
    public Task task;

    public TaskCreatedEvent(Task t) {
        task = t;
    }
}
