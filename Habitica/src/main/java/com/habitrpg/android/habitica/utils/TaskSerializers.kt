package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.habitrpg.shared.habitica.serialization.JsonTreeSerializer
import com.habitrpg.shared.habitica.serialization.array
import com.habitrpg.shared.habitica.serialization.bool
import com.habitrpg.shared.habitica.serialization.decode
import com.habitrpg.shared.habitica.serialization.double
import com.habitrpg.shared.habitica.serialization.encode
import com.habitrpg.shared.habitica.serialization.int
import com.habitrpg.shared.habitica.serialization.obj
import com.habitrpg.shared.habitica.serialization.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put
import java.util.Date

object TaskSerializer : JsonTreeSerializer<Task>("Task") {
    override fun Json.read(element: JsonElement): Task {
        val task = Task()
        val obj = element as? JsonObject ?: return task
        task.text = obj.string("text") ?: ""
        task.notes = obj.string("notes") ?: ""
        task.ownerID = obj.string("userId") ?: ""
        task.value = obj.double("value") ?: 0.0
        task.type = TaskType.from(obj.string("type")) ?: TaskType.HABIT
        task.frequency = Frequency.from(obj.string("frequency"))
        task.attribute = Attribute.from(obj.string("attribute"))
        task.everyX = obj.int("everyX")
        task.priority = obj.double("priority")?.toFloat() ?: 1.0f
        task.completed = obj.bool("completed") ?: false
        task.up = obj.bool("up") ?: false
        task.down = obj.bool("down") ?: false
        task.streak = obj.int("streak")
        obj.obj("challenge")?.let { challenge ->
            challenge.string("id")?.let {
                task.challengeID = it
                task.challengeBroken = challenge.string("broken")
            }
        }
        task.counterUp = obj.int("counterUp")
        task.counterDown = obj.int("counterDown")
        task.dateCreated = decode(obj["createdAt"])
        task.dueDate = decode(obj["date"])
        task.updatedAt = decode(obj["updatedAt"])
        task.startDate = decode(obj["startDate"])
        task.isDue = obj.bool("isDue")
        obj.array("nextDue")?.forEach { due -> decode<Date>(due)?.let { task.nextDue?.add(it) } }
        obj.array("checklist")?.forEach {
            val item = it as JsonObject
            task.checklist?.add(ChecklistItem(item.string("id") ?: "", item.string("text") ?: "", item.bool("completed") ?: false))
        }
        obj.array("reminders")?.forEach {
            val item = it as JsonObject
            task.reminders?.add(
                RemindersItem().apply {
                    id = item.string("id") ?: ""
                    startDate = item.string("startDate") ?: ""
                    time = item.string("time") ?: ""
                },
            )
        }
        if (obj.containsKey("repeat")) task.repeat = decode<Days>(obj["repeat"])
        obj.obj("group")?.let { groupObject ->
            val group = decode<TaskGroupPlan>(groupObject) ?: return@let
            val approval = groupObject.obj("approval")
            if (group.groupID?.isNotBlank() == true && approval != null) {
                // Anything but a JSON boolean counts as false
                fun approvalFlag(key: String) = (approval[key] as? JsonPrimitive)?.takeUnless { it.isString }?.booleanOrNull ?: false
                if (approval.containsKey("requested")) group.approvalRequested = approvalFlag("requested")
                if (approval.containsKey("approved")) group.approvalApproved = approvalFlag("approved")
                if (approval.containsKey("required")) group.approvalRequired = approvalFlag("required")
            }
            task.group = group
        }
        // Realm can't store lists of ints, so these are kept as strings on the task
        obj.intList("weeksOfMonth")?.let { task.setWeeksOfMonth(it) }
        obj.intList("daysOfMonth")?.let { task.setDaysOfMonth(it) }
        // Set last, since the setter also updates combinedID
        task.id = obj.string("_id") ?: ""
        return task
    }

    private fun JsonObject.intList(key: String): List<Int>? =
        array(key)?.takeIf { it.isNotEmpty() }?.map { (it as JsonPrimitive).doubleOrNull?.toInt() ?: 0 }

    override fun Json.write(value: Task): JsonElement =
        buildJsonObject {
            put("_id", value.id)
            put("text", value.text)
            put("notes", value.notes)
            put("value", value.value)
            put("priority", value.priority)
            put("attribute", value.attribute?.value)
            put("type", value.type?.value)
            put("tags", buildJsonArray { value.tags?.forEach { add(JsonPrimitive(it.id)) } })
            when (value.type) {
                TaskType.HABIT -> {
                    put("up", value.up)
                    put("down", value.down)
                    put("frequency", value.frequency?.value)
                    put("counterUp", value.counterUp)
                    put("counterDown", value.counterDown)
                }

                TaskType.DAILY -> {
                    put("frequency", value.frequency?.value)
                    put("everyX", value.everyX)
                    put("repeat", encode(value.repeat))
                    put("startDate", encode(value.startDate))
                    put("streak", value.streak)
                    value.checklist?.let { put("checklist", checklist(it)) }
                    put("reminders", value.reminders?.let { reminders(it, withType = true) } ?: JsonNull)
                    put("daysOfMonth", encode(value.getDaysOfMonth()))
                    put("weeksOfMonth", encode(value.getWeeksOfMonth()))
                    put("completed", value.completed)
                }

                TaskType.TODO -> {
                    put("date", value.dueDate?.let { encode(it) } ?: JsonPrimitive(""))
                    value.checklist?.let { put("checklist", checklist(it)) }
                    value.reminders?.let { put("reminders", reminders(it, withType = false)) }
                    put("completed", value.completed)
                }

                else -> {}
            }
        }

    private fun checklist(items: List<ChecklistItem>): JsonArray =
        buildJsonArray {
            items.forEach { item ->
                add(
                    buildJsonObject {
                        put("text", item.text)
                        put("id", item.id)
                        put("completed", item.completed)
                    },
                )
            }
        }

    // Dailies have always sent every reminder field, to-dos only the ones that are set
    private fun reminders(
        items: List<RemindersItem>,
        withType: Boolean,
    ): JsonArray =
        buildJsonArray {
            items.forEach { item ->
                add(
                    buildJsonObject {
                        put("id", item.id)
                        if (withType || item.startDate != null) put("startDate", item.startDate)
                        put("time", item.time)
                        if (withType) put("type", item.type)
                    },
                )
            }
        }
}

/** The server sends tasks as an array, the app keeps them by ID. */
object TaskListSerializer : JsonTreeSerializer<TaskList>("TaskList") {
    override fun Json.read(element: JsonElement): TaskList {
        val tasks = mutableMapOf<String, Task>()
        (element as? JsonArray)?.forEach { taskElement ->
            val obj = taskElement as? JsonObject ?: return@forEach
            val task = decodeFromJsonElement(TaskSerializer, obj)
            task.tags?.addAll(tags(obj.array("tags")))
            task.id?.let { tasks[it] = task }
        }
        return TaskList().apply { this.tasks = tasks }
    }

    // Raw tag-id entries become thin (id-only) placeholders; TaskRepositoryImpl resolves them against local tags after parsing.
    private fun Json.tags(json: JsonArray?): List<Tag> {
        val tags = mutableListOf<Tag>()
        json?.forEach { tagElement ->
            if (tagElement is JsonObject) {
                decode<Tag>(tagElement)?.let { tags.add(it) }
            } else {
                val tagId = (tagElement as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content ?: return@forEach
                if (tags.none { it.id == tagId }) tags.add(Tag().apply { id = tagId })
            }
        }
        return tags
    }
}
