package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viirus on 09/12/15.
 */
public class TaskListDeserializer implements JsonDeserializer<List<Task>> {

    public List<Task> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        List<Task> vals = new ArrayList<Task>();
        int position = 0;
        for (JsonElement e : json.getAsJsonArray()) {
            Task task = (Task) ctx.deserialize(e, Task.class);
            task.position = position;
            vals.add(task);
            position++;
        }
        return vals;
    }
}
