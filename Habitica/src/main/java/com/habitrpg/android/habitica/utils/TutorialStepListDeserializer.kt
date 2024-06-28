package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.TutorialStep
import io.realm.RealmList
import java.lang.reflect.Type

class TutorialStepListDeserializer : JsonDeserializer<List<TutorialStep>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<TutorialStep> {
        val steps = RealmList<TutorialStep>()
        for (group in listOf("common", "android")) {
            if (json.asJsonObject.has(group)) {
                for (entry in json.asJsonObject.get(group).asJsonObject.entrySet()) {
                    steps.add(parseStep(group, entry))
                }
            }
        }

        return steps
    }

    private fun parseStep(
        group: String,
        entry: MutableMap.MutableEntry<String, JsonElement>
    ): TutorialStep {
        val article = TutorialStep()
        article.tutorialGroup = group
        article.identifier = entry.key
        article.wasCompleted = entry.value.asBoolean
        return article
    }
}
