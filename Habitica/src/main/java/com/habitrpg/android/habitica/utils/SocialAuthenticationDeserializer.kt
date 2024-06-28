package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication
import java.lang.reflect.Type

class SocialAuthenticationDeserializer : JsonDeserializer<SocialAuthentication> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SocialAuthentication {
        val authentication = SocialAuthentication()
        val obj = json.asJsonObject
        if (obj.has("emails") && obj.get("emails").isJsonArray) {
            val emailJson = obj.getAsJsonArray("emails")
            for (entry in emailJson) {
                if (entry.isJsonPrimitive) {
                    authentication.emails.add(entry.asString)
                } else if (entry.isJsonObject) {
                    authentication.emails.add(entry.asJsonObject.getAsString("value"))
                }
            }
        }
        return authentication
    }
}
