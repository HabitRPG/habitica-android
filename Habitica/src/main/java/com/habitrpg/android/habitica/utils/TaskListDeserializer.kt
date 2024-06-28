package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import io.realm.Realm
import io.realm.RealmList
import java.lang.reflect.Type

class TaskListDeserializer : JsonDeserializer<TaskList> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): TaskList {
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
            // Tests don't have a database
            databaseTags = ArrayList()
        }

        for (e in json.asJsonArray) {
            try {
                val obj = e as? JsonObject
                if (obj != null) {
                    val task = ctx.deserialize<Task>(obj, Task::class.java)
                    task.tags = handleTags(databaseTags, obj.getAsJsonArray("tags"), ctx)
                    task.id?.let { taskMap[it] = task }
                }
            } catch (ignored: ClassCastException) {
            } catch (ignored: java.lang.UnsupportedOperationException) {
            }
        }

        tasks.tasks = taskMap
        deserializeTrace.stop()
        return tasks
    }

    private fun handleTags(
        databaseTags: List<Tag>,
        json: JsonArray?,
        context: JsonDeserializationContext
    ): RealmList<Tag> {
        val tags = RealmList<Tag>()
        for (tagElement in json ?: listOf<JsonElement>()) {
            if (tagElement.isJsonObject) {
                tags.add(context.deserialize(tagElement, Tag::class.java))
            } else {
                try {
                    val tagId = tagElement.asString
                    for (tag in databaseTags) {
                        if (tag.id == tagId) {
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

    private fun alreadyContainsTag(
        list: List<Tag>,
        idToCheck: String
    ): Boolean {
        for (t in list) {
            if (t.id == idToCheck) {
                return true
            }
        }

        return false
    }
}
