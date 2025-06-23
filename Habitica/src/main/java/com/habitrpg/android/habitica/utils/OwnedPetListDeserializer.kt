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
        val entrySet = json?.asJsonObject?.entrySet() ?: return ownedItems

        for (entry in entrySet) {
            val item = OwnedPet().apply { key = entry.key }

            // safely coerce whatever the server sent into an Int (default 0)
            val trainedCount = entry.value
                .takeIf { it.isJsonPrimitive }
                ?.asJsonPrimitive
                ?.let { prim ->
                    when {
                        prim.isNumber   -> prim.asInt
                        prim.isBoolean  -> if (prim.asBoolean) 1 else 0
                        prim.isString   -> prim.asString.toIntOrNull() ?: 0
                        else            -> 0
                    }
                } ?: 0

            item.trained = trainedCount
            ownedItems.add(item)
        }

        return ownedItems
    }
}
