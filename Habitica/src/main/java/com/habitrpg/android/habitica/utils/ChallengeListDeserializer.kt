package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.social.Challenge
import io.realm.RealmList
import java.lang.reflect.Type

class ChallengeListDeserializer : JsonDeserializer<List<Challenge>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): List<Challenge> {
        val challenges = RealmList<Challenge>()

        for (element in json.asJsonArray) {
            var challenge: Challenge?
            if (element.isJsonObject) {
                challenge = context.deserialize<Challenge>(element, Challenge::class.java)
            } else {
                challenge = Challenge()
                challenge.id = element.asString
            }
            if (challenge != null) {
                challenges.add(challenge)
            }
        }

        return challenges
    }
}
