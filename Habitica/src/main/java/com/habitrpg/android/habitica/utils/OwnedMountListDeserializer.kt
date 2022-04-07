package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.user.OwnedMount
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedMountListDeserializer : JsonDeserializer<List<OwnedMount>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<OwnedMount> {
        val ownedItems = RealmList<OwnedMount>()
        val entrySet = json?.asJsonObject?.entrySet()
        if (entrySet != null) {
            for (entry in entrySet) {
                val item = OwnedMount()
                item.key = entry.key
                if (entry.value.isJsonNull) {
                    item.owned = false
                } else {
                    item.owned = entry.value.asBoolean
                }
                ownedItems.add(item)
            }
        }
        return ownedItems
    }
}
