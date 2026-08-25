package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.Equipment
import io.realm.RealmList
import java.lang.reflect.Type

class EquipmentListDeserializer : JsonDeserializer<RealmList<Equipment?>?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): RealmList<Equipment?> {
        val vals = RealmList<Equipment?>()
        if (json.isJsonObject) {
            for ((_, value) in json.getAsJsonObject().entrySet()) {
                vals.add(context.deserialize(value, Equipment::class.java))
            }
        } else {
            for (item in json.getAsJsonArray()) {
                vals.add(
                    context.deserialize(
                        item.getAsJsonObject(),
                        Equipment::class.java,
                    ),
                )
            }
        }

        return vals
    }
}
