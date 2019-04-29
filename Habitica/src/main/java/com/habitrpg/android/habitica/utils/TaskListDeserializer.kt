package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.gson.*
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.*
import io.realm.Realm
import io.realm.RealmList

import java.lang.reflect.Type
import java.util.*

/**
 * Created by viirus on 09/12/15.
 */
class TaskListDeserializer : JsonDeserializer<TaskList> {

    private fun getIntListFromJsonArray(jsonArray: JsonArray): List<Int> {
        val intList = ArrayList<Int>()

        for (i in 0 until jsonArray.size()) {
            intList.add(jsonArray.get(i).asInt)
        }

        return intList
    }

    private fun getMonthlyDays(e: JsonElement, task: Task) {
        val weeksOfMonth = e.asJsonObject.getAsJsonArray("weeksOfMonth")
        if (weeksOfMonth != null && weeksOfMonth.size() > 0) {
            task.setWeeksOfMonth(getIntListFromJsonArray(weeksOfMonth))
        }

        val daysOfMonth = e.asJsonObject.getAsJsonArray("daysOfMonth")
        if (weeksOfMonth != null && weeksOfMonth.size() > 0) {
            task.setDaysOfMonth(getIntListFromJsonArray(daysOfMonth))
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): TaskList {
        val tasks = TaskList()
        val taskMap = HashMap<String, Task>()
        val deserializeTrace = FirebasePerformance.getInstance().newTrace("TaskListDeserialize")
        deserializeTrace.start()
        var databaseTags: List<Tag>
        try {
            val realm = Realm.getDefaultInstance()
            databaseTags = realm.copyFromRealm(realm.where(Tag::class.java).findAll())
            realm.close()
        } catch (e: RuntimeException) {
            //Tests don't have a database
            databaseTags = ArrayList()
        }

        for (e in json.asJsonArray) {
            try {
                val obj = e as? JsonObject
                if (obj != null) {
                    val task = Task()
                    task.text = obj.getAsString("text")
                    task.notes = obj.getAsString("notes")
                    task.userId = obj.getAsString("userId")
                    task.value = obj.get("value")?.asDouble ?: 0.0
                    task.type = obj.getAsString("type")
                    task.frequency = obj.getAsString("frequency")
                    task.attribute = obj.getAsString("attribute")
                    task.everyX = obj.get("everyX")?.asInt
                    task.priority = obj.get("priority")?.asFloat ?: 1.0f
                    task.completed = obj.get("completed")?.asBoolean ?: false
                    task.up = obj.get("up")?.asBoolean ?: false
                    task.down = obj.get("down")?.asBoolean ?: false
                    task.streak = obj.get("streak")?.asInt
                    task.counterUp = obj.get("counterUp")?.asInt
                    task.counterDown = obj.get("counterDown")?.asInt
                    task.dateCreated = ctx.deserialize(obj.get("createdAt"), Date::class.java)
                    task.dueDate = ctx.deserialize(obj.get("date"), Date::class.java)
                    task.startDate = ctx.deserialize(obj.get("startDate"), Date::class.java)
                    task.isDue = obj.get("isDue")?.asBoolean
                    if (obj.has("nextDue")) {
                        task.nextDue = RealmList()
                        for (due in obj.getAsJsonArray("nextDue")) {
                            task.nextDue?.add(ctx.deserialize(due, Date::class.java))
                        }
                    }
                    task.tags = handleTags(databaseTags, obj.getAsJsonArray("tags"), ctx)
                    if (obj.has("checklist")) {
                        task.checklist = RealmList()
                        for (checklistElement in obj.getAsJsonArray("checklist")) {
                            val checklistObject = checklistElement.asJsonObject
                            task.checklist?.add(ChecklistItem(
                                    checklistObject.getAsString("id"),
                                    checklistObject.getAsString("text"),
                                    checklistObject.get("completed").asBoolean
                            ))
                        }
                    }
                    if (obj.has("reminders")) {
                        task.reminders = RealmList()
                        for (reminderElement in obj.getAsJsonArray("reminders")) {
                            val remindersObject = reminderElement.asJsonObject
                            val reminder = RemindersItem()
                            reminder.id = remindersObject.getAsString("id")
                            reminder.startDate = ctx.deserialize(remindersObject.get("startDate"), Date::class.java)
                            reminder.time = ctx.deserialize(remindersObject.get("time"), Date::class.java)
                            task.reminders?.add(reminder)
                        }
                    }
                    if (obj.has("repeat")) {
                        task.repeat = ctx.deserialize(obj.get("repeat"), Days::class.java)
                    }

                    if (obj.has("group")) {
                        val group = TaskGroupPlan()
                        val groupObject = obj.getAsJsonObject("group")
                        val approvalObject = groupObject.getAsJsonObject("approval")
                        group.approvalRequested = approvalObject.getAsJsonPrimitive("requested").asBoolean
                        group.approvalApproved = approvalObject.getAsJsonPrimitive("approved").asBoolean
                        group.approvalRequired = approvalObject.getAsJsonPrimitive("required").asBoolean
                        task.group = group
                    }
                    // Work around since Realm does not support Arrays of ints
                    getMonthlyDays(e, task)

                    //Workaround, since gson doesn't call setter methods
                    task.id = obj.getAsString("_id")
                    task.id?.let { taskMap[it] = task }
                }
            } catch (ignored: ClassCastException) {

            }
        }

        tasks.tasks = taskMap
        deserializeTrace.stop()
        return tasks
    }

    private fun handleTags(databaseTags: List<Tag>, json: JsonArray?, context: JsonDeserializationContext): RealmList<Tag>? {
        val tags = RealmList<Tag>()
        for (tagElement in json ?: listOf<JsonElement>()) {
            if (tagElement.isJsonObject) {
                tags.add(context.deserialize(tagElement, Tag::class.java))
            } else {
                try {
                    val tagId = tagElement.asString
                    for (tag in databaseTags) {
                        if (tag.getId() == tagId) {
                            if (!alreadyContainsTag(tags, tagId)) {
                                tags.add(tag)
                            }

                            break
                        }
                    }
                } catch (ignored: UnsupportedOperationException) {

                }

            }
        }
        return tags
    }

    private fun alreadyContainsTag(list: List<Tag>, idToCheck: String): Boolean {
        for (t in list) {
            if (t.getId() == idToCheck) {
                return true
            }
        }

        return false
    }
}
