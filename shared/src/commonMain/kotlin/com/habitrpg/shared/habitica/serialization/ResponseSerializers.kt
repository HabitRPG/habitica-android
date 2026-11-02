package com.habitrpg.shared.habitica.serialization

import com.habitrpg.shared.habitica.models.Notification
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/** The shape of `data` depends on the notification type. */
object NotificationSerializer : JsonTreeSerializer<Notification>("Notification") {
    override fun Json.read(element: JsonElement): Notification {
        val obj = element as JsonObject
        val notification = Notification()
        obj.string("id")?.let { notification.id = it }
        notification.type = obj.string("type")
        notification.seen = obj.bool("seen")
        val dataSerializer = notification.getDataSerializer()
        val data = obj.obj("data")
        if (data != null && dataSerializer != null) {
            // Notification data is free-form on the server, so one unexpected shape must not fail the whole response
            notification.data = runCatching { decodeFromJsonElement(dataSerializer, data) }.getOrNull()
        }
        return notification
    }
}

object FeedResponseSerializer : JsonTreeSerializer<FeedResponse>("FeedResponse") {
    override fun Json.read(element: JsonElement) = FeedResponse().apply { value = (element as? JsonPrimitive)?.intOrNull }
}
