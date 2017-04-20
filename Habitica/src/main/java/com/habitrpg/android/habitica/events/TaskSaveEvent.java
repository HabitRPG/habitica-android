package com.habitrpg.android.habitica.events;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

/**
 * Created by viirus on 03/08/15.
 */
public class TaskSaveEvent {
    public Task task;
    public boolean created;
    public boolean ignoreEvent;
}
