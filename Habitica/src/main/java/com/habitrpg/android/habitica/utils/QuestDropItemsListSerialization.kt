package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import io.realm.RealmList
import java.lang.reflect.Type

class QuestDropItemsListSerialization : JsonDeserializer<RealmList<QuestDropItem>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): RealmList<QuestDropItem> {
        val items = RealmList<QuestDropItem>()
        val keys = ArrayList<String>()

        for (e in json.asJsonArray) {
            val item = context.deserialize<QuestDropItem>(e, QuestDropItem::class.java)
            if (keys.contains(item.key)) {
                for (existingItem in items) {
                    if (existingItem.key == item.key) {
                        existingItem.count = existingItem.count + 1
                    }
                }
            } else {
                item.count = 1
                items.add(item)
                keys.add(item.key)
            }
        }

        return items
    }
}
