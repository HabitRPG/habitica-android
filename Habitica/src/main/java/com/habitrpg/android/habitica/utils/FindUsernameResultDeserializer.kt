package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.ContributorInfo
import java.lang.reflect.Type

class FindUsernameResultDeserializer : JsonDeserializer<FindUsernameResult> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FindUsernameResult {
        val obj = json.asJsonObject

        val result = FindUsernameResult()

        if (obj.has("contributor")) {
            result.contributor =
                context.deserialize<ContributorInfo>(
                    obj.get("contributor"),
                    ContributorInfo::class.java
                )
        }
        if (obj.has("auth")) {
            result.authentication =
                context.deserialize<Authentication>(obj.get("auth"), Authentication::class.java)
        }

        return result
    }
}
