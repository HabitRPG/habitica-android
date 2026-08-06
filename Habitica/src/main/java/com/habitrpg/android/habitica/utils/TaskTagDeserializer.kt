package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.Tag
import java.lang.reflect.Type

class TaskTagDeserializer : JsonDeserializer<List<Tag>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): List<Tag> {
        val tags = mutableListOf<Tag>()

        if (json.isJsonArray) {
            for (tagElement in json.asJsonArray) {
                if (tagElement.isJsonObject) {
                    tags.add(context.deserialize<Tag>(tagElement, Tag::class.java))
                } else {
                    try {
                        val tagId = tagElement.asString
                        if (!alreadyContainsTag(tags, tagId)) {
                            tags.add(Tag().apply { id = tagId })
                        }
                    } catch (ignored: UnsupportedOperationException) {
                    }
                }
            }
        }

        return tags
    }

    private fun alreadyContainsTag(
        list: List<Tag>,
        idToCheck: String,
    ): Boolean {
        for (t in list) {
            if (t.id == idToCheck) {
                return true
            }
        }

        return false
    }
}
