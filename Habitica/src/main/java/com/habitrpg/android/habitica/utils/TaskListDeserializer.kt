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
import java.lang.reflect.Type

class TaskListDeserializer : JsonDeserializer<TaskList> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext,
    ): TaskList {
        val tasks = TaskList()
        val taskMap = HashMap<String, Task>()
        val deserializeTrace =
            try {
                FirebasePerformance.getInstance().newTrace("TaskListDeserialize")
            } catch (ignored: IllegalStateException) {
                // Firebase isn't initialized outside a running app process (e.g. unit tests)
                null
            }
        deserializeTrace?.start()

        for (e in json.asJsonArray) {
            try {
                val obj = e as? JsonObject
                if (obj != null) {
                    val task = ctx.deserialize<Task>(obj, Task::class.java)
                    task.tags?.addAll(handleTags(obj.getAsJsonArray("tags"), ctx))
                    task.id?.let { taskMap[it] = task }
                }
            } catch (ignored: ClassCastException) {
            } catch (ignored: java.lang.UnsupportedOperationException) {
            }
        }

        tasks.tasks = taskMap
        deserializeTrace?.stop()
        return tasks
    }

    // Raw tag-id entries become thin (id-only) placeholders; TaskRepositoryImpl resolves them against local tags after parsing.
    private fun handleTags(
        json: JsonArray?,
        context: JsonDeserializationContext,
    ): List<Tag> {
        val tags = mutableListOf<Tag>()
        for (tagElement in json ?: listOf<JsonElement>()) {
            if (tagElement.isJsonObject) {
                tags.add(context.deserialize(tagElement, Tag::class.java))
            } else {
                try {
                    val tagId = tagElement.asString
                    if (tags.none { it.id == tagId }) {
                        tags.add(Tag().apply { id = tagId })
                    }
                } catch (ignored: UnsupportedOperationException) {
                }
            }
        }
        return tags
    }
}
