package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskTag;

import java.lang.reflect.Type;

public class TaskSerializer implements JsonSerializer<Task> {
    @Override
    public JsonElement serialize(Task task, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("_id", task.getId());
        obj.addProperty("text", task.getText());
        obj.addProperty("notes", task.getNotes());
        obj.addProperty("value", task.getValue());
        obj.addProperty("priority", task.getPriority());
        obj.addProperty("attribute", task.getAttribute());
        obj.addProperty("type", task.getType());
        JsonArray tagsList = new JsonArray();
        for (Tag tag : task.getTags()) {
            tagsList.add(tag.getId());
        }
        obj.add("tags", tagsList);
        switch (task.getType()) {
            case "habit":
                obj.addProperty("up", task.getUp());
                obj.addProperty("down", task.getDown());
                break;
            case "daily":
                obj.addProperty("frequency", task.getFrequency());
                obj.addProperty("everyX", task.getEveryX());
                obj.add("repeat", context.serialize(task.getRepeat()));
                obj.add("startDate", context.serialize(task.getStartDate()));
                obj.addProperty("streak", task.getStreak());
                obj.add("checklist", context.serialize(task.getChecklist()));
                obj.add("reminders", context.serialize(task.getReminders()));
                obj.addProperty("completed", task.getCompleted());
                break;
            case "todo":
                if (task.getDueDate() == null) {
                    obj.addProperty("date", "");
                } else {
                    obj.add("date", context.serialize(task.getDueDate()));
                }
                obj.add("checklist", context.serialize(task.getChecklist()));
                obj.add("reminders", context.serialize(task.getReminders()));
                obj.addProperty("completed", task.getCompleted());
                break;
        }

        return obj;
    }
}
