package com.habitrpg.android.habitica.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.members.MemberPreferences
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.user.*
import io.realm.Realm
import java.lang.reflect.Type

class MemberSerialization : JsonDeserializer<Member> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Member {
        val obj = json.asJsonObject
        val id = obj.get("_id").asString

        val realm = Realm.getDefaultInstance()
        var member = realm.where(Member::class.java).equalTo("id", id).findFirst() ?: Member()
        if (member.id == null) {
            member.id = id
        } else {
            member = realm.copyFromRealm(member)
        }

        if (obj.has("stats")) {
            member.stats = context.deserialize<Stats>(obj.get("stats"), Stats::class.java)
        }
        if (obj.has("inbox")) {
            member.inbox = context.deserialize<Inbox>(obj.get("inbox"), Inbox::class.java)
        }
        if (obj.has("preferences")) {
            member.preferences = context.deserialize<MemberPreferences>(obj.get("preferences"), MemberPreferences::class.java)
        }
        if (obj.has("profile")) {
            member.profile = context.deserialize<Profile>(obj.get("profile"), Profile::class.java)
        }
        if (obj.has("party")) {
            member.party = context.deserialize<UserParty>(obj.get("party"), UserParty::class.java)
            if (member.party != null && member.party?.quest != null) {
                member.party?.quest?.id = member.id
                if (!obj.get("party").asJsonObject.get("quest").asJsonObject.has("RSVPNeeded")) {
                    val quest = realm.where(Quest::class.java).equalTo("id", member.id).findFirst()
                    if (quest != null && quest.isValid) {
                        member.party?.quest?.RSVPNeeded = quest.RSVPNeeded
                    }
                }
            }
        }

        if (obj.has("items")) {
            member.items = context.deserialize(obj.get("items"), Items::class.java)
        }
        if (obj.has("contributor")) {
            member.contributor = context.deserialize<ContributorInfo>(obj.get("contributor"), ContributorInfo::class.java)
        }
        if (obj.has("auth")) {
            member.authentication = context.deserialize<Authentication>(obj.get("auth"), Authentication::class.java)
        }
        if (obj.has("loginIncentives")) {
            member.loginIncentives = obj.get("loginIncentives").asInt
        }

        member.id = member.id

        realm.close()
        return member
    }
}
