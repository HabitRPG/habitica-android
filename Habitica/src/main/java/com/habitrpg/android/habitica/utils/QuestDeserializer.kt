package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestMember
import com.habitrpg.android.habitica.models.inventory.QuestProgress
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import io.realm.RealmList
import java.lang.reflect.Type

class QuestDeserializer : JsonDeserializer<Quest> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Quest {
        val obj = json.asJsonObject
        val quest = Quest()

        if (obj.has("progress")) {
            val progress = QuestProgress()
            progress.key = quest.key
            val progressObj = obj.getAsJsonObject("progress")
            if (progressObj.has("hp")) {
                progress.hp = progressObj.get("hp").asDouble
            }
            if (progressObj.has("rage")) {
                progress.rage = progressObj.get("rage").asDouble
            }
            if (progressObj.has("up")) {
                progress.up = progressObj.get("up").asFloat
            }
            if (progressObj.has("down")) {
                progress.down = progressObj.get("down").asFloat
            }
            if (progressObj.has("collectedItems")) {
                progress.collectedItems = progressObj.get("collectedItems").asInt
            }
            if (progressObj.has("collect")) {
                progress.collect = RealmList()
                for ((key, value) in progressObj.getAsJsonObject("collect").entrySet()) {
                    val collect = QuestProgressCollect()
                    collect.key = key
                    collect.count = value.asInt
                    progress.collect?.add(collect)
                }
            }
            quest.progress = progress
        }

        if (obj.has("key") && !obj.get("key").isJsonNull) {
            quest.key = obj.get("key").asString
        } else {
            return quest
        }
        if (obj.has("active")) {
            quest.active = obj.get("active").asBoolean
        }
        if (obj.has("leader")) {
            quest.leader = obj.get("leader").asString
        }
        if (obj.has("RSVPNeeded")) {
            quest.RSVPNeeded = obj.get("RSVPNeeded").asBoolean
        }

        if (obj.has("members")) {
            val members = RealmList<QuestMember>()
            for ((key, value) in obj.getAsJsonObject("members").entrySet()) {
                val member = QuestMember()
                member.key = key
                if (value.isJsonNull) {
                    member.isParticipating = null
                } else {
                    member.isParticipating = value.asBoolean
                }
                members.add(member)
            }
            quest.members = members
        }
        return quest
    }
}
