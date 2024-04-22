package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.Achievement
import java.lang.reflect.Type

class AchievementListDeserializer : JsonDeserializer<List<Achievement?>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): List<Achievement?> {
        val achievements = mutableListOf<Achievement>()
        for (categoryEntry in json?.asJsonObject?.entrySet() ?: emptySet()) {
            val categoryIdentifier = categoryEntry.key
            for (entry in categoryEntry.value.asJsonObject.getAsJsonObject("achievements")
                .entrySet()) {
                val obj = entry.value.asJsonObject
                val achievement = Achievement()
                achievement.key = entry.key
                achievement.category = categoryIdentifier
                achievement.earned = obj.get("earned").asBoolean
                achievement.title = obj.getAsString("title")
                achievement.text = obj.getAsString("text")
                achievement.icon = obj.getAsString("icon")
                achievement.index = if (obj.has("index")) obj["index"].asInt else 0
                achievement.optionalCount =
                    if (obj.has("optionalCount")) obj["optionalCount"].asInt else 0
                achievements.add(achievement)
            }
        }
        return achievements
    }
}
