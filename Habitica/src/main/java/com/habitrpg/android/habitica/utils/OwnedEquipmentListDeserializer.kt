package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.user.OwnedEquipment
import io.realm.RealmList
import java.lang.reflect.Type

class OwnedEquipmentListDeserializer : JsonDeserializer<RealmList<OwnedEquipment?>?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): RealmList<OwnedEquipment?> {
        val ownedEquipment = RealmList<OwnedEquipment?>()
        val entrySet = json.asJsonObject?.entrySet()
        if (entrySet != null) {
            for ((key, value) in entrySet) {
                if (value.isJsonPrimitive) {
                    val item = OwnedEquipment()
                    item.key = key
                    item.owned = value.asBoolean
                    ownedEquipment.add(item)
                }
            }
        }

        return ownedEquipment
    }
}
