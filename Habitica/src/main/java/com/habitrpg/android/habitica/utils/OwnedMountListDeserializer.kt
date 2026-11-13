package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.user.OwnedMount
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedMountListDeserializer : JsonDeserializer<RealmList<OwnedMount>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): RealmList<OwnedMount> {
        val ownedItems = RealmList<OwnedMount>()
        val entrySet = json?.asJsonObject?.entrySet()
        if (entrySet != null) {
            for ((key, value) in entrySet) {
                val item = OwnedMount()
                item.key = key
                item.owned = !value.isJsonNull && value.asBoolean
                ownedItems.add(item)
            }
        }
        return ownedItems
    }
}
