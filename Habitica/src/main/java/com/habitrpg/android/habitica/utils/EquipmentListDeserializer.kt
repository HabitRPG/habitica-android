package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.Equipment
import io.realm.Realm
import java.lang.reflect.Type

class EquipmentListDeserializer : JsonDeserializer<MutableList<Equipment?>?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): MutableList<Equipment?> {
        val vals = mutableListOf<Equipment?>()
        if (json.isJsonObject) {
            val obj = json.getAsJsonObject()

            val realm = Realm.getDefaultInstance()
            val existingItems = realm.copyFromRealm(
                realm.where(Equipment::class.java).findAll()
            )
            realm.close()

            for (item in existingItems) {
                if (obj.has(item.key)) {
                    val itemObject = obj.get(item.key)

                    if (itemObject.isJsonObject) {
                        val parsedItem = context.deserialize<Equipment>(
                            itemObject.getAsJsonObject(),
                            Equipment::class.java
                        )
                        item.text = parsedItem.text
                        item.value = parsedItem.value
                        item.type = parsedItem.type
                        item.klass = parsedItem.klass
                        item.specialClass = parsedItem.specialClass
                        item.index = parsedItem.index
                        item.notes = parsedItem.notes
                        item.con = parsedItem.con
                        item.str = parsedItem.str
                        item.per = parsedItem.per
                        item.intelligence = parsedItem.intelligence
                        item.twoHanded = parsedItem.twoHanded
                        item.mystery = parsedItem.mystery
                        item.gearSet = parsedItem.gearSet
                    } else {
                        item.owned = itemObject.asBoolean
                    }
                    vals.add(item)
                    obj.remove(item.key)
                }
            }

            for (entry in json.getAsJsonObject().entrySet()) {
                val item: Equipment?
                if (entry.value.isJsonObject) {
                    item = context.deserialize<Equipment?>(entry.value, Equipment::class.java)
                } else {
                    item = Equipment()
                    item.key = entry.key
                    if (entry.value.isJsonNull) {
                        item.owned = false
                    } else {
                        item.owned = entry.value.asBoolean
                    }
                }
                vals.add(item)
            }
        } else {
            for (item in json.getAsJsonArray()) {
                vals.add(
                    context.deserialize<Equipment?>(
                        item.getAsJsonObject(),
                        Equipment::class.java
                    )
                )
            }
        }

        return vals
    }
}
