package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.notifications.ChatNotificationData
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.models.notifications.NotificationType
import java.lang.reflect.Type

class NotificationDeserializer : JsonDeserializer<GlobalNotification> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GlobalNotification {
        val notification = GlobalNotification()
        val obj = json.asJsonObject

        if (obj.has("id")) {
            notification.id = obj.get("id").asString
        }

        if (obj.has("type")) {
            notification.type = obj.get("type").asString
        }

        if (obj.has("seen")) {
            notification.seen = obj.get("seen").asBoolean
        }

        if (obj.has("data")) {
            when (notification.type) {
                NotificationType.NEW_CHAT_MESSAGE.type -> notification.newChatMessageData = context.deserialize<ChatNotificationData>(
                        obj.getAsJsonObject("data"),
                        ChatNotificationData::class.java
                )
            }
        }

        return notification
    }
}
