package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import java.lang.reflect.Type

class FeedResponseDeserializer : JsonDeserializer<FeedResponse> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FeedResponse {
        val response = FeedResponse()
        response.value = json.asInt
        return response
    }
}
