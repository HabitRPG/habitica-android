package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.SpriteSubstitutionContext
import io.realm.RealmList
import java.lang.reflect.Type

class SpriteSubstitutionContextListDeserializer: JsonDeserializer<RealmList<SpriteSubstitutionContext>> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): RealmList<SpriteSubstitutionContext> {
            val contexts = RealmList<SpriteSubstitutionContext>()
            for ((key, subs) in json.asJsonObject.asMap()) {
                val context = SpriteSubstitutionContext()
                context.key = key
                for (sub in subs.asJsonObject.asMap()) {
                    context.substitutions[sub.key] = sub.value.asString
                }
                if (context.substitutions.isNotEmpty()) {
                    contexts.add(context)
                }
            }

            return contexts
        }
}