package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.habitrpg.android.habitica.extensions.getAsString
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.WorldStateEvent
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import io.realm.RealmList
import java.lang.reflect.Type

class WorldStateSerialization : JsonDeserializer<WorldState> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): WorldState {
        val state = WorldState()
        val obj = json?.asJsonObject ?: return state
        val worldBossObject = obj.get("worldBoss")?.asJsonObject
        if (worldBossObject != null) {
            if (worldBossObject.has("active") && !worldBossObject["active"].isJsonNull) {
                state.worldBossActive = worldBossObject["active"].asBoolean
            }
            if (worldBossObject.has("key") && !worldBossObject["key"].isJsonNull) {
                state.worldBossKey = worldBossObject["key"].asString
            }
            if (worldBossObject.has("progress")) {
                val progress = QuestProgress()
                val progressObj = worldBossObject.getAsJsonObject("progress")
                if (progressObj.has("hp")) {
                    progress.hp = progressObj["hp"].asDouble
                }
                if (progressObj.has("rage")) {
                    progress.rage = progressObj["rage"].asDouble
                }
                state.progress = progress
            }
            if (worldBossObject.has("extra")) {
                val extra = worldBossObject["extra"].asJsonObject
                if (extra.has("worldDmg")) {
                    val worldDmg = extra["worldDmg"].asJsonObject
                    state.rageStrikes = RealmList()
                    worldDmg.entrySet().forEach { (key, value) ->
                        val strike = QuestRageStrike(key, value.asBoolean)
                        state.rageStrikes?.add(strike)
                    }
                }
            }
        }

        state.npcImageSuffix = obj.getAsString("npcImageSuffix")

        try {
            if (obj.has("currentEvent") && obj.get("currentEvent")?.isJsonObject == true) {
                val event = obj.getAsJsonObject("currentEvent")
                if (event != null) {
                    state.currentEvent = context?.deserialize(event, WorldStateEvent::class.java)
                }
                if (obj.has("currentEventList")) {
                    val events = RealmList<WorldStateEvent>()
                    for (element in obj.getAsJsonArray("currentEventList")) {
                        context?.deserialize<WorldStateEvent>(element, WorldStateEvent::class.java)?.let { events.add(it) }
                    }
                    state.events = events
                }
            }
        } catch (_: Exception) {
        }

        return state
    }
}
