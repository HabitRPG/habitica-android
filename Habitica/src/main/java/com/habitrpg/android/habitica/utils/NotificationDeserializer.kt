package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.notifications.*
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

        val dataType = notification.getDataType()
        if (obj.has("data") && dataType != null) {
            notification.setData(
                    context.deserialize(obj.getAsJsonObject("data"), dataType)
            )
        }

        return notification
    }
}
