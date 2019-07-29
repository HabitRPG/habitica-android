package com.habitrpg.android.habitica.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.lang.reflect.Type;
import java.util.List;

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
        if (task.getTags() != null) {
            for (Tag tag : task.getTags()) {
                tagsList.add(tag.getId());
            }
        }
        obj.add("tags", tagsList);
        switch (task.getType()) {
            case "habit":
                obj.addProperty("up", task.getUp());
                obj.addProperty("down", task.getDown());
                obj.addProperty("frequency", task.getFrequency());
                obj.addProperty("counterUp", task.getCounterUp());
                obj.addProperty("counterDown", task.getCounterDown());
                break;
            case "daily":
                obj.addProperty("frequency", task.getFrequency());
                obj.addProperty("everyX", task.getEveryX());
                obj.add("repeat", context.serialize(task.getRepeat()));
                obj.add("startDate", context.serialize(task.getStartDate()));
                obj.addProperty("streak", task.getStreak());
                if (task.getChecklist() != null) {
                    obj.add("checklist", serializeChecklist(task.getChecklist()));
                }
                if (task.getReminders() != null) {
                    obj.add("reminders", serializeReminders(task.getReminders()));
                }
                obj.add("reminders", context.serialize(task.getReminders()));
                obj.add("daysOfMonth", context.serialize(task.getDaysOfMonth()));
                obj.add("weeksOfMonth", context.serialize(task.getWeeksOfMonth()));
                obj.addProperty("completed", task.getCompleted());
                break;
            case "todo":
                if (task.getDueDate() == null) {
                    obj.addProperty("date", "");
                } else {
                    obj.add("date", context.serialize(task.getDueDate()));
                }
                if (task.getChecklist() != null) {
                    obj.add("checklist", serializeChecklist(task.getChecklist()));
                }
                if (task.getReminders() != null) {
                    obj.add("reminders", serializeReminders(task.getReminders()));
                }
                obj.addProperty("completed", task.getCompleted());
                break;
        }

        return obj;
    }

    private JsonArray serializeChecklist(List<ChecklistItem> checklist) {
        JsonArray jsonArray = new JsonArray();
        for (ChecklistItem item : checklist) {
            JsonObject object = new JsonObject();
            object.addProperty("text", item.getText());
            object.addProperty("id", item.getId());
            object.addProperty("completed", item.getCompleted());
            jsonArray.add(object);
        }
        return jsonArray;
    }

    private JsonArray serializeReminders(List<RemindersItem> reminders) {
        JsonArray jsonArray = new JsonArray();
        for (RemindersItem item : reminders) {
            JsonObject object = new JsonObject();
            object.addProperty("id", item.getId());
            if (item.getStartDate() != null) {
                object.addProperty("startDate", item.getStartDate().getTime());
            }
            object.addProperty("time", item.getTime().getTime());
            jsonArray.add(object);
        }
        return jsonArray;
    }
}
