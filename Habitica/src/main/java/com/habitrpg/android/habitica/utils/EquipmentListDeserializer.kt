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
            for ((key, value) in json.getAsJsonObject().entrySet()) {
                val item: Equipment?
                if (value.isJsonObject) {
                    item = context.deserialize(value, Equipment::class.java)
                } else {
                    item = Equipment()
                    item.key = key
                    item.owned = !value.isJsonNull && value.asBoolean
                }
                vals.add(item)
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
