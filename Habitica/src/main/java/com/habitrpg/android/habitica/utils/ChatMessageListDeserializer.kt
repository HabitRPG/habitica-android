package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.social.ChatMessage

import java.lang.reflect.Type

import io.realm.RealmList

class ChatMessageListDeserializer : JsonDeserializer<RealmList<ChatMessage>> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RealmList<ChatMessage> {
        val messages = RealmList<ChatMessage>()

        if (json.isJsonArray) {
            json.asJsonArray.mapTo(messages) { context.deserialize(it, ChatMessage::class.java) }
        } else {
            for ((_, value) in json.asJsonObject.entrySet()) {
                messages.add(context.deserialize(value, ChatMessage::class.java))
            }
        }
        //Make sure the messageId is set for all likes
        messages.forEach { it.id = it.id }

        return messages
    }
}
