package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.social.Backer
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.ChatMessageLike
import com.habitrpg.android.habitica.models.user.ContributorInfo
import io.realm.RealmList
import java.lang.reflect.Type

class ChatMessageDeserializer : JsonDeserializer<ChatMessage> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChatMessage {
        val message = ChatMessage()
        val obj = json.asJsonObject
        if (obj.has("id")) {
            message.id = obj.get("id").asString
        }
        if (obj.has("text") && !obj.get("text").isJsonNull && obj.get("text").isJsonPrimitive) {
            message.text = obj.get("text").asString
        }
        if (obj.has("timestamp")) {
            message.timestamp = obj.get("timestamp").asLong
        }
        if (obj.has("likes")) {
            message.likes = RealmList()
            for ((key, value) in obj.getAsJsonObject("likes").entrySet()) {
                if (value.asBoolean) {
                    message.likes?.add(ChatMessageLike(key, message.id))
                }
            }
        }
        if (obj.has("flagCount")) {
            message.flagCount = obj.get("flagCount").asInt
        }

        if (obj.has("uuid")) {
            message.uuid = obj.get("uuid").asString
        }

        if (obj.has("contributor")) {
            if (!obj.get("contributor").isJsonNull) {
                if (obj.get("contributor").isJsonObject) {
                    message.contributor = context.deserialize<ContributorInfo>(obj.get("contributor"), ContributorInfo::class.java)
                } else {
                    val contributor = ContributorInfo()
                    contributor.text = obj.get("contributor").asString
                    message.contributor = contributor
                }
                message.contributor?.userId = message.id
            }
        }

        if (obj.has("backer")) {
            message.backer = context.deserialize<Backer>(obj.get("backer"), Backer::class.java)
        }

        if (obj.has("user")) {
            message.user = obj.get("user").asString
        }

        if (obj.has("sent")) {
            message.sent = obj.get("sent").asString
        }

        return message
    }
}
