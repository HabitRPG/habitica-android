package com.habitrpg.android.habitica.utils

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.realm.RealmList
import java.lang.reflect.Type
import java.util.Date

private val JsonPrimitive.asBooleanOrFalse: Boolean
    get() =
        if (isBoolean) {
            asBoolean
        } else {
            false
        }

class TaskSerializer : JsonSerializer<Task>, JsonDeserializer<Task> {
    private fun getIntListFromJsonArray(jsonArray: JsonArray): List<Int> {
        val intList = ArrayList<Int>()

        for (i in 0 until jsonArray.size()) {
            intList.add(jsonArray.get(i).asInt)
        }

        return intList
    }

    private fun getMonthlyDays(
        e: JsonObject,
        task: Task,
    ) {
        val weeksOfMonth = e.getAsJsonArray("weeksOfMonth")
        if (weeksOfMonth != null && weeksOfMonth.size() > 0) {
            task.setWeeksOfMonth(getIntListFromJsonArray(weeksOfMonth))
        }

        val daysOfMonth = e.getAsJsonArray("daysOfMonth")
        if (daysOfMonth != null && daysOfMonth.size() > 0) {
            task.setDaysOfMonth(getIntListFromJsonArray(daysOfMonth))
        }
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): Task {
        val task = Task()
        val obj = json as? JsonObject ?: return task
        task.text = obj.getAsString("text")
        task.notes = obj.getAsString("notes")
        task.ownerID = obj.getAsString("userId")
        task.value = obj.get("value")?.asDouble ?: 0.0
        task.type = TaskType.from(obj.getAsString("type")) ?: TaskType.HABIT
        task.frequency = Frequency.from(obj.getAsString("frequency"))
        task.attribute = Attribute.from(obj.getAsString("attribute"))
        task.everyX = obj.get("everyX")?.asInt
        task.priority = obj.get("priority")?.asFloat ?: 1.0f
        task.completed = obj.get("completed")?.asBoolean ?: false
        task.up = obj.get("up")?.asBoolean ?: false
        task.down = obj.get("down")?.asBoolean ?: false
        task.streak = obj.safeGet("streak")?.asInt
        if (obj.getAsJsonObject("challenge").has("id")) {
            task.challengeID = obj.getAsJsonObject("challenge").get("id").asString

            if (obj.getAsJsonObject("challenge").has("broken")) {
                task.challengeBroken = obj.getAsJsonObject("challenge").get("broken").asString
            }
        }
        try {
            task.counterUp = obj.get("counterUp")?.asInt
            task.counterDown = obj.get("counterDown")?.asInt
        } catch (ignored: java.lang.UnsupportedOperationException) {
        }
        task.dateCreated = context.deserialize(obj.get("createdAt"), Date::class.java)
        task.dueDate = context.deserialize(obj.get("date"), Date::class.java)
        task.updatedAt = context.deserialize(obj.get("updatedAt"), Date::class.java)
        task.startDate = context.deserialize(obj.get("startDate"), Date::class.java)
        task.isDue = obj.get("isDue")?.asBoolean
        if (obj.has("nextDue")) {
            task.nextDue = RealmList()
            for (due in obj.getAsJsonArray("nextDue")) {
                task.nextDue?.add(context.deserialize(due, Date::class.java))
            }
        }
        if (obj.has("checklist")) {
            task.checklist = RealmList()
            for (checklistElement in obj.getAsJsonArray("checklist")) {
                val checklistObject = checklistElement.asJsonObject
                task.checklist?.add(
                    ChecklistItem(
                        checklistObject.getAsString("id"),
                        checklistObject.getAsString("text"),
                        checklistObject.get("completed").asBoolean,
                    ),
                )
            }
        }
        if (obj.has("reminders")) {
            task.reminders = RealmList()
            for (reminderElement in obj.getAsJsonArray("reminders")) {
                val remindersObject = reminderElement.asJsonObject
                val reminder = RemindersItem()
                reminder.id = remindersObject.getAsString("id")
                reminder.startDate = remindersObject.getAsString("startDate")
                reminder.time = remindersObject.getAsString("time")
                task.reminders?.add(reminder)
            }
        }
        if (obj.has("repeat")) {
            task.repeat = context.deserialize(obj.get("repeat"), Days::class.java)
        }

        if (obj.has("group")) {
            val groupObject = obj.getAsJsonObject("group")
            val group: TaskGroupPlan = context.deserialize(groupObject, TaskGroupPlan::class.java)
            if (group.groupID?.isNotBlank() == true && groupObject.has("approval")) {
                val approvalObject = groupObject.getAsJsonObject("approval")
                if (approvalObject.has("requested")) {
                    group.approvalRequested =
                        approvalObject.getAsJsonPrimitive("requested").asBooleanOrFalse
                }
                if (approvalObject.has("approved")) {
                    group.approvalApproved =
                        approvalObject.getAsJsonPrimitive("approved").asBooleanOrFalse
                }
                if (approvalObject.has("required")) {
                    group.approvalRequired =
                        approvalObject.getAsJsonPrimitive("required").asBooleanOrFalse
                }
            }
            task.group = group
        }
        // Work around since Realm does not support Arrays of ints
        getMonthlyDays(obj, task)

        // Workaround, since gson doesn't call setter methods
        task.id = obj.getAsString("_id")
        return task
    }

    override fun serialize(
        task: Task,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("_id", task.id)
        obj.addProperty("text", task.text)
        obj.addProperty("notes", task.notes)
        obj.addProperty("value", task.value)
        obj.addProperty("priority", task.priority)
        obj.addProperty("attribute", task.attribute?.value)
        obj.addProperty("type", task.type?.value)
        val tagsList = JsonArray()
        task.tags?.forEach { tag ->
            tagsList.add(tag.id)
        }
        obj.add("tags", tagsList)
        when (task.type) {
            TaskType.HABIT -> {
                obj.addProperty("up", task.up)
                obj.addProperty("down", task.down)
                obj.addProperty("frequency", task.frequency?.value)
                obj.addProperty("counterUp", task.counterUp)
                obj.addProperty("counterDown", task.counterDown)
            }

            TaskType.DAILY -> {
                obj.addProperty("frequency", task.frequency?.value)
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

            TaskType.TODO -> {
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

            else -> {
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
                jsonObject.addProperty("startDate", item.startDate)
            }
            jsonObject.addProperty("time", item.time)
            jsonArray.add(jsonObject)
        }
        return jsonArray
    }
}

private fun JsonObject.safeGet(key: String): JsonElement? {
    if (has("streak") && !get(key).isJsonNull) {
        return get(key)
    }
    return null
}
