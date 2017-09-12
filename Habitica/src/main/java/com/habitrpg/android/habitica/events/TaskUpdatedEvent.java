package com.habitrpg.android.habitica.events;

import com.habitrpg.android.habitica.models.tasks.Task;

/**
 * Created by Negue on 28.09.2015.
 */
public class TaskUpdatedEvent {
    public Task task;

    public TaskUpdatedEvent(Task t) {
        task = t;
    }
}
