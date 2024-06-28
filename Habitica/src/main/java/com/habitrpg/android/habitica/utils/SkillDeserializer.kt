package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.Skill
import java.lang.reflect.Type

class SkillDeserializer : JsonDeserializer<List<Skill>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): List<Skill> {
        val jsonObject = json.asJsonObject
        val skills = ArrayList<Skill>()
        for ((classname, value) in jsonObject.entrySet()) {
            for ((_, value1) in value.asJsonObject.entrySet()) {
                val skillObject = value1.asJsonObject
                val skill = Skill()
                skill.key = skillObject.get("key").asString
                skill.text = skillObject.get("text").asString
                skill.notes = skillObject.get("notes").asString
                skill.key = skillObject.get("key").asString
                skill.target = skillObject.get("target").asString
                skill.habitClass = classname
                skill.mana = skillObject.get("mana").asInt

                val lvlElement = skillObject.get("lvl")

                if (lvlElement != null) {
                    skill.lvl = lvlElement.asInt
                }

                skills.add(skill)
            }
        }

        return skills
    }
}
