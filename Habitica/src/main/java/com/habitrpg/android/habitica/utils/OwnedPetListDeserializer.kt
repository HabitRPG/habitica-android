package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.user.OwnedPet
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedPetListDeserializer : JsonDeserializer<List<OwnedPet>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<OwnedPet> {
        val ownedItems = RealmList<OwnedPet>()
        val entrySet = json?.asJsonObject?.entrySet()
        if (entrySet != null) {
            for (entry in entrySet) {
                val item = OwnedPet()
                item.key = entry.key
                item.trained = entry.value.asInt
                ownedItems.add(item)
            }
        }
        return ownedItems
    }
}
