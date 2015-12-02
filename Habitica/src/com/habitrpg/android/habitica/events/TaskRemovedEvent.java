package com.habitrpg.android.habitica.events;

/**
 * Created by Negue on 01.12.2015.
 */
public class TaskRemovedEvent {
    public String deletedTaskId;

    public TaskRemovedEvent(String id) {
        deletedTaskId = id;
    }
}
