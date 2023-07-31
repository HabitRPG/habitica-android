package com.habitrpg.android.habitica.utils

import android.text.TextUtils
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.social.Challenge
import java.lang.reflect.Type
import java.util.Date

class ChallengeDeserializer : JsonDeserializer<Challenge>, JsonSerializer<Challenge> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Challenge {
        val jsonObject = json.asJsonObject

        val challenge = Challenge()

        challenge.id = jsonObject.get("id").asString
        challenge.name = jsonObject.get("name").asString
        challenge.shortName = jsonObject.getAsString("shortName")
        challenge.description = jsonObject.getAsString("description")
        challenge.memberCount = jsonObject.get("memberCount").asInt

        val prizeElement = jsonObject.get("prize")
        if (!prizeElement.isJsonNull) {
            challenge.prize = prizeElement.asInt
        }

        challenge.official = jsonObject.get("official").asBoolean

        val leaderElement = jsonObject.get("leader")

        if (leaderElement != null && !leaderElement.isJsonNull) {
            val leaderObj = leaderElement.asJsonObject

            if (leaderObj != null) {
                val profile = leaderObj.get("profile").asJsonObject

                if (profile != null) {
                    challenge.leaderName = profile.get("name").asString

                    var id: JsonElement? = leaderObj.get("id")
                    if (id == null) {
                        id = leaderObj.get("_id")
                    }

                    if (id != null) {
                        challenge.leaderId = id.asString
                    }
                }
            }
        }

        if (jsonObject.has("createdAt")) {
            challenge.createdAt = context.deserialize(jsonObject.get("createdAt"), Date::class.java)
        }
        if (jsonObject.has("updatedAt")) {
            challenge.updatedAt = context.deserialize(jsonObject.get("updatedAt"), Date::class.java)
        }

        if (jsonObject.has("summary")) {
            challenge.summary = jsonObject.get("summary").asString
        }

        val groupElement = jsonObject.get("group")

        if (groupElement != null && !groupElement.isJsonNull) {
            val groupObj = groupElement.asJsonObject

            if (groupObj != null) {
                challenge.groupName = groupObj.get("name").asString
                challenge.groupId = groupObj.get("_id").asString
            }
        }

        val tasksOrderElement = jsonObject.get("tasksOrder")

        if (tasksOrderElement != null && !tasksOrderElement.isJsonNull) {
            val tasksOrderObj = tasksOrderElement.asJsonObject

            challenge.todoList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_TODOS)
            challenge.dailyList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_DAILYS)
            challenge.habitList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_HABITS)
            challenge.rewardList = getTaskArrayAsString(context, tasksOrderObj, Challenge.TASK_ORDER_REWARDS)
        }

        return challenge
    }

    private fun getTaskArrayAsString(
        context: JsonDeserializationContext,
        tasksOrderObj: JsonObject,
        taskType: String
    ): String {
        if (tasksOrderObj.has(taskType)) {
            val jsonElement = tasksOrderObj.get(taskType)

            val taskArray = context.deserialize<Array<String>>(jsonElement, Array<String>::class.java)

            return TextUtils.join(",", taskArray)
        }

        return ""
    }

    override fun serialize(src: Challenge, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("id", src.id)
        obj.addProperty("name", src.name)
        obj.addProperty("shortName", src.shortName)
        obj.addProperty("description", src.description)
        obj.addProperty("memberCount", src.memberCount)
        obj.addProperty("prize", src.prize)
        obj.addProperty("official", src.official)

        obj.addProperty("group", src.groupId)
        obj.add("tasksOrder", context.serialize(src.tasksOrder))

        return obj
    }
}
