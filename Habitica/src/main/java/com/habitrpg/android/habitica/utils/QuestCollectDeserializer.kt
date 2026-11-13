package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.QuestCollect
import io.realm.RealmList
import java.lang.reflect.Type

class QuestCollectDeserializer : JsonDeserializer<RealmList<QuestCollect>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): RealmList<QuestCollect> {
        val items = RealmList<QuestCollect>()

        for ((key, value) in json.asJsonObject.entrySet()) {
            val questCollect = QuestCollect()
            questCollect.key = key
            val jsonObject = value.asJsonObject
            questCollect.count = jsonObject.get("count").asInt
            questCollect.text = jsonObject.get("text").asString
            items.add(questCollect)
        }

        return items
    }
}
