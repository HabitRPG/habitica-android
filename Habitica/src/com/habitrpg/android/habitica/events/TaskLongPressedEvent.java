package com.habitrpg.android.habitica.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.habitrpg.android.habitica.TaskFormActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Negue on 10.07.2015.
 */
public class TaskLongPressedEvent {
    private final Context context;
    private final String taskType;
    private final List<String> tags;
    public com.magicmicky.habitrpgwrapper.lib.models.tasks.Task task;

    public TaskLongPressedEvent(Context context, String taskType, List<String> tags) {
        this.context = context;
        this.taskType = taskType;
        this.tags = tags;
    }


    public void openNewTaskActivity() {
        if (taskType == "daily" || taskType == "todo") {
            Bundle bundle = new Bundle();
            bundle.putString("type", taskType);
            bundle.putString("taskId", task.getId());
            bundle.putStringArrayList("tagsId", new ArrayList<String>(tags));

            Intent intent = new Intent(context, TaskFormActivity.class);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            context.startActivity(intent);
        }
    }
}
