package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            taskMap.put(task.getId(), task);
        }

        tasks.tasks = taskMap;

        return tasks;
    }
}
