package com.habitrpg.android.habitica.utils

import com.google.gson.*
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import java.lang.reflect.Type
import java.util.*

class TaskSerializer : JsonSerializer<Task> {
    override fun serialize(task: Task, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("_id", task.id)
        obj.addProperty("text", task.text)
        obj.addProperty("notes", task.notes)
        obj.addProperty("value", task.value)
        obj.addProperty("priority", task.priority)
        obj.addProperty("attribute", task.attribute)
        obj.addProperty("type", task.type)
        val tagsList = JsonArray()
        task.tags?.forEach { tag ->
            tagsList.add(tag.id)
        }
        obj.add("tags", tagsList)
        when (task.type) {
            "habit" -> {
                obj.addProperty("up", task.up)
                obj.addProperty("down", task.down)
                obj.addProperty("frequency", task.frequency)
                obj.addProperty("counterUp", task.counterUp)
                obj.addProperty("counterDown", task.counterDown)
            }
            "daily" -> {
                obj.addProperty("frequency", task.frequency)
                obj.addProperty("everyX", task.everyX)
                obj.add("repeat", context.serialize(task.repeat))
                obj.add("startDate", context.serialize(task.startDate))
                obj.addProperty("streak", task.streak)
                if (task.checklist != null) {
                    obj.add("checklist", serializeChecklist(task.checklist))
                }
                if (task.reminders != null) {
                    obj.add("reminders", serializeReminders(task.reminders))
                }
                obj.add("reminders", context.serialize(task.reminders))
                obj.add("daysOfMonth", context.serialize(task.getDaysOfMonth()))
                obj.add("weeksOfMonth", context.serialize(task.getWeeksOfMonth()))
                obj.addProperty("completed", task.completed)
            }
            "todo" -> {
                if (task.dueDate == null) {
                    obj.addProperty("date", "")
                } else {
                    obj.add("date", context.serialize(task.dueDate))
                }
                if (task.checklist != null) {
                    obj.add("checklist", serializeChecklist(task.checklist))
                }
                if (task.reminders != null) {
                    obj.add("reminders", serializeReminders(task.reminders))
                }
                obj.addProperty("completed", task.completed)
            }
        }

        return obj
    }

    private fun serializeChecklist(checklist: List<ChecklistItem>?): JsonArray {
        val jsonArray = JsonArray()
        checklist?.forEach { item ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("text", item.text)
            jsonObject.addProperty("id", item.id)
            jsonObject.addProperty("completed", item.completed)
            jsonArray.add(jsonObject)
        }
        return jsonArray
    }

    private fun serializeReminders(reminders: List<RemindersItem>?): JsonArray {
        val jsonArray = JsonArray()
        reminders?.forEach { item ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("id", item.id)
            if (item.startDate != null) {
                jsonObject.addProperty("startDate", item.startDate?.time)
            }
            jsonObject.addProperty("time", item.time?.time ?: Date().time)
            jsonArray.add(jsonObject)
        }
        return jsonArray
    }
}
