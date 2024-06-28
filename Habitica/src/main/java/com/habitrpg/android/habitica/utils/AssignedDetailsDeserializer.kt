package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.tasks.GroupAssignedDetails
import io.realm.RealmList
import java.lang.reflect.Type

class AssignedDetailsDeserializer : JsonDeserializer<RealmList<GroupAssignedDetails?>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RealmList<GroupAssignedDetails?> {
        val list = RealmList<GroupAssignedDetails?>()
        if (json == null) {
            return list
        } else if (json.isJsonArray) {
            json.asJsonArray.forEach {
                list.add(
                    context?.deserialize(
                        it,
                        GroupAssignedDetails::class.java
                    )
                )
            }
        } else {
            val jsonObject = json.asJsonObject
            jsonObject.keySet().forEach {
                val details =
                    context?.deserialize<GroupAssignedDetails>(
                        jsonObject[it],
                        GroupAssignedDetails::class.java
                    )
                details?.assignedUserID = it
                list.add(details)
            }
        }
        return list
    }
}
