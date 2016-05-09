package com.habitrpg.android.habitica.callbacks;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import rx.functions.Action1;


/**
 * Created by magicmicky on 02/04/15.
 */
public class TaskDeletionCallback implements Action1<Void> {
    private final OnTaskDeleted callback;
    private final Task taskToDelete;

    public TaskDeletionCallback(OnTaskDeleted cb, Task taskToDelete) {
        this.callback = cb;
        this.taskToDelete = taskToDelete;
    }

    @Override
    public void call(Void aVoid) {
        callback.onTaskDeleted(taskToDelete);
    }


    public interface OnTaskDeleted {
        public void onTaskDeleted(Task deleted);
    }
}
