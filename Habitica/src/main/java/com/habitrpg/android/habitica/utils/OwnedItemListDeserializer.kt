package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.user.OwnedItem
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedItemListDeserializer : JsonDeserializer<List<OwnedItem>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): List<OwnedItem> {
        val ownedItems = RealmList<OwnedItem>()
        val entrySet = json?.asJsonObject?.entrySet()
        if (entrySet != null) {
            for (entry in entrySet) {
                if (entry.value.isJsonPrimitive) {
                    val item = OwnedItem()
                    item.key = entry.key
                    item.numberOwned = entry.value.asInt
                    ownedItems.add(item)
                }
            }
        }
        return ownedItems
    }
}
