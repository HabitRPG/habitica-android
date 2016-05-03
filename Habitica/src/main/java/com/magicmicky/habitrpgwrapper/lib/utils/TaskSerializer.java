package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;

import java.lang.reflect.Type;

public class TaskSerializer implements JsonSerializer<Task> {
    @Override
    public JsonElement serialize(Task task, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", task.getId());
        obj.addProperty("text", task.getText());
        obj.addProperty("notes", task.getNotes());
        obj.addProperty("value", task.getValue());
        obj.addProperty("priority", task.getPriority());
        obj.addProperty("attribute", task.getAttribute());
        obj.addProperty("type", task.getType());
        JsonObject tagsObj = new JsonObject();
        for (TaskTag tag : task.getTags()) {
            tagsObj.addProperty(tag.getTag().getId(), true);
        }
        obj.add("tags", tagsObj);
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
                obj.addProperty("completed", task.getCompleted());
                break;
            case "todo":
                if (task.getDueDate() == null) {
                    obj.addProperty("date", "");
                } else {
                    obj.add("date", context.serialize(task.getDueDate()));
                }
                obj.add("checklist", context.serialize(task.getChecklist()));
                obj.addProperty("completed", task.getCompleted());
                break;
        }

        return obj;
    }
}
