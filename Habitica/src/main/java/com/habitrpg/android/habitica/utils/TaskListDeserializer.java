package com.habitrpg.android.habitica.utils;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by viirus on 09/12/15.
 */
public class TaskListDeserializer implements JsonDeserializer<TaskList> {

    public TaskList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        TaskList tasks = new TaskList();
        Map<String, Task> taskMap = new HashMap<>();

        for (JsonElement e : json.getAsJsonArray()) {
            Task task = ctx.deserialize(e, Task.class);

            JsonArray weeksOfMonth = e.getAsJsonObject().getAsJsonArray("weeksOfMonth");
            if (weeksOfMonth != null) {
                task.weeksOfMonth = weeksOfMonth.toString();
            }

            JsonArray daysOfMonth = e.getAsJsonObject().getAsJsonArray("daysOfMonth");
            if (weeksOfMonth != null) {
                task.daysOfMonth = daysOfMonth.toString();
            }

            taskMap.put(task.getId(), task);
        }

        tasks.tasks = taskMap;

        return tasks;
    }
}
