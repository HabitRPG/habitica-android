package com.habitrpg.android.habitica.utils

import com.google.gson.*
import com.habitrpg.android.habitica.models.tasks.Task
import java.lang.reflect.Type

class TaskDeserializer : JsonDeserializer<Task> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Task {
        val task = Task()
        val obj = json.asJsonObject

        task.text = obj.getStringOrEmpty("text")
        task.notes = obj.getStringOrEmpty("notes")

        task.value = obj.getOrNull("value")?.asDouble ?: 0.0
        task.type = obj.getStringOrEmpty("type")
        task.attribute = obj.getStringOrEmpty("attribute")
        task.isDue = obj.getOrNull("isDue")?.asBoolean ?: false
        task.yesterDaily = obj.getOrNull("yesterDaily")?.asBoolean ?: false
        return task
    }
}

private fun JsonObject.getOrNull(key: String): JsonElement? {
    return if (has(key) && !get(key).isJsonNull) get(key) else null
}

private fun JsonObject.getStringOrNull(key: String): String? {
    return getOrNull(key)?.asString
}

private fun JsonObject.getStringOrEmpty(key: String): String {
    return getOrNull(key)?.asString ?: ""
}