package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestRageStrike
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.social.GroupCategory
import com.habitrpg.common.habitica.models.tasks.TasksOrder
import io.realm.Realm
import io.realm.RealmList
import java.lang.reflect.Type

class GroupSerialization : JsonDeserializer<Group>, JsonSerializer<Group> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Group {
        val group = Group()
        val obj = json.asJsonObject
        group.id = obj.get("_id").asString
        group.name = obj.get("name").asString
        if (obj.has("description") && !obj.get("description").isJsonNull) {
            group.description = obj.get("description").asString
        }
        if (obj.has("summary") && !obj.get("summary").isJsonNull) {
            group.summary = obj.get("summary").asString
        }
        if (obj.has("leaderMessage") && !obj.get("leaderMessage").isJsonNull) {
            group.leaderMessage = obj.get("leaderMessage").asString
        }
        if (obj.has("privacy")) {
            group.privacy = obj.get("privacy").asString
        }
        if (obj.has("memberCount")) {
            group.memberCount = obj.get("memberCount").asInt
        }
        if (obj.has("balance")) {
            group.balance = obj.get("balance").asDouble
        }
        if (obj.has("logo") && !obj.get("logo").isJsonNull) {
            group.logo = obj.get("logo").asString
        }
        if (obj.has("type")) {
            group.type = obj.get("type").asString
        }
        if (obj.has("leader")) {
            if (obj.get("leader").isJsonPrimitive) {
                group.leaderID = obj.get("leader").asString
            } else {
                val leader = obj.get("leader").asJsonObject
                group.leaderID = leader.get("_id").asString
                if (leader.has("profile") && !leader.get("profile").isJsonNull) {
                    if (leader.get("profile").asJsonObject.has("name")) {
                        group.leaderName = leader.get("profile").asJsonObject.get("name").asString
                    }
                }
            }
        }
        if (obj.has("quest")) {
            group.quest = context.deserialize(obj.get("quest"), object : TypeToken<Quest>() {}.type)
            group.quest?.id = group.id
            val questObject = obj.getAsJsonObject("quest")
            if (questObject.has("members")) {
                val members = obj.getAsJsonObject("quest").getAsJsonObject("members")
                val realm = Realm.getDefaultInstance()
                val dbMembers = realm.copyFromRealm(realm.where(Member::class.java).equalTo("party.id", group.id).findAll())
                realm.close()
                dbMembers.forEach { member ->
                    if (members.has(member.id)) {
                        val value = members.get(member.id)
                        if (value.isJsonNull) {
                            member.participatesInQuest = null
                        } else {
                            member.participatesInQuest = value.asBoolean
                        }
                    } else {
                        member.participatesInQuest = null
                    }
                    members.remove(member.id)
                }
                members.entrySet().forEach { (key, value) ->
                    val member = Member()
                    member.id = key
                    if (!value.isJsonNull) {
                        member.participatesInQuest = value.asBoolean
                    }
                    dbMembers.add(member)
                }
                val newMembers = RealmList<Member>()
                newMembers.addAll(dbMembers)
                group.quest?.participants = newMembers
            }

            if (questObject.has("extra") && questObject["extra"].asJsonObject.has("worldDmg")) {
                val worldDamageObject = questObject.getAsJsonObject("extra").getAsJsonObject("worldDmg")
                worldDamageObject.entrySet().forEach { (key, value) ->
                    val rageStrike = QuestRageStrike(key, value.asBoolean)
                    group.quest?.addRageStrike(rageStrike)
                }
            }
        }

        if (obj.has("leaderOnly")) {
            val leaderOnly = obj.getAsJsonObject("leaderOnly")
            if (leaderOnly.has("challenges")) {
                group.leaderOnlyChallenges = leaderOnly.get("challenges").asBoolean
            }
            if (leaderOnly.has("getGems")) {
                group.leaderOnlyGetGems = leaderOnly.get("getGems").asBoolean
            }
        }

        if (obj.has("tasksOrder")) {
            group.tasksOrder = context.deserialize(obj.get("tasksOrder"), TasksOrder::class.java)
        }

        if (obj.has("categories")) {
            group.categories = RealmList()
            obj.getAsJsonArray("categories").forEach {
                group.categories?.add(context.deserialize(it, GroupCategory::class.java))
            }
        }

        return group
    }

    override fun serialize(src: Group, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj.addProperty("name", src.name)
        obj.addProperty("description", src.description)
        obj.addProperty("summary", src.summary)
        obj.addProperty("logo", src.logo)
        obj.addProperty("type", src.type)
        obj.addProperty("type", src.type)
        obj.addProperty("leader", src.leaderID)
        val leaderOnly = JsonObject()
        leaderOnly.addProperty("challenges", src.leaderOnlyChallenges)
        leaderOnly.addProperty("getGems", src.leaderOnlyGetGems)
        obj.add("leaderOnly", leaderOnly)
        return obj
    }
}
