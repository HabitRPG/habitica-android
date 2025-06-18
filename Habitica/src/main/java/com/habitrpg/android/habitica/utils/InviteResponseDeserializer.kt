package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializer
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import io.realm.RealmList
import java.lang.reflect.Type

class InviteResponseDeserializer : JsonDeserializer<InviteResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): InviteResponse {
        return when {
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                InviteResponse.EmailInvite(json.asString)
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                InviteResponse.UserInvite(
                    id      = obj["id"].asString,
                    name    = obj["name"].asString,
                    inviter = obj["inviter"].asString
                )
            }
            else -> throw JsonParseException("Unexpected InviteResponse: $json")
        }
    }
}