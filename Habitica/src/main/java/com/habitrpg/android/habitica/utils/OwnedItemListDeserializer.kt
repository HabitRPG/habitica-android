package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.user.OwnedItem
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedItemListDeserializer : JsonDeserializer<RealmList<OwnedItem>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): RealmList<OwnedItem> {
        val ownedItems = RealmList<OwnedItem>()
        val entrySet = json?.asJsonObject?.entrySet()
        if (entrySet != null) {
            for ((key, value) in entrySet) {
                if (value.isJsonPrimitive) {
                    val item = OwnedItem()
                    item.key = key
                    item.numberOwned = value.asInt
                    ownedItems.add(item)
                }
            }
        }
        return ownedItems
    }
}
