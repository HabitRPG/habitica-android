package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by viirus on 09/12/15.
 */
public class TaskListDeserializer implements JsonDeserializer<TaskList> {

    private List<Integer> getIntListFromJsonArray(JsonArray jsonArray) {
        List<Integer> intList = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            intList.add(jsonArray.get(i).getAsInt());
        }

        return intList;
    }

    private void getMonthlyDays(JsonElement e, Task task) {
        JsonArray weeksOfMonth = e.getAsJsonObject().getAsJsonArray("weeksOfMonth");
        if (weeksOfMonth != null && weeksOfMonth.size() > 0) {
            task.setWeeksOfMonth(getIntListFromJsonArray(weeksOfMonth));
        }

        JsonArray daysOfMonth = e.getAsJsonObject().getAsJsonArray("daysOfMonth");
        if (weeksOfMonth != null && weeksOfMonth.size() > 0) {
            task.setDaysOfMonth(getIntListFromJsonArray(daysOfMonth));
        }
    }

    public TaskList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        TaskList tasks = new TaskList();
        Map<String, Task> taskMap = new HashMap<>();

        for (JsonElement e : json.getAsJsonArray()) {
            try {
                Task task = ctx.deserialize(e, Task.class);

                // Work around since Realm does not support Arrays of ints
                getMonthlyDays(e, task);

                //Workaround, since gson doesn't call setter methods
                task.setId(task.getId());
                taskMap.put(task.getId(), task);
            } catch (ClassCastException ignored) {

            }
        }

        tasks.tasks = taskMap;

        return tasks;
    }
}
