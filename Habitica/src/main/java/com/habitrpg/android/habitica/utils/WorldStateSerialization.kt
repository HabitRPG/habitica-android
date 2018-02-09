package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import java.lang.reflect.Type

class WorldStateSerialization: JsonDeserializer<WorldState> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorldState {
        val worldBossObject = json?.asJsonObject?.get("worldBoss")?.asJsonObject
        val state = WorldState()
        if (worldBossObject == null) {
            return state
        }
        state.worldBossActive = worldBossObject["active"].asBoolean
        state.worldBossKey = worldBossObject["key"].asString
        if (worldBossObject.has("progress")) {
            val progress = QuestProgress()
            val progressObj = worldBossObject.getAsJsonObject("progress")
            if (progressObj.has("hp")) {
                progress.hp = progressObj["hp"].asDouble
            }
            if (progressObj.has("rage")) {
                progress.rage = progressObj["rage"].asDouble
            }
        }
        if (worldBossObject.has("extra")) {
            val extra = worldBossObject["extra"].asJsonObject
            if (extra.has("worldDmg")) {
                val worldDmg = extra["worldDmg"].asJsonObject
                state.rageStrikes = mutableListOf()
                worldDmg.entrySet().forEach { (key, value) ->
                    val strike = QuestRageStrike(key, value.asBoolean)
                    state.rageStrikes?.add(strike)
                }
            }
        }
        return state
    }

}