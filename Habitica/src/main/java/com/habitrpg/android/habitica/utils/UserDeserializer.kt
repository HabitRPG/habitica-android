package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.models.PushDevice
import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.invitations.Invitations
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.social.UserParty
import com.habitrpg.android.habitica.models.tasks.TasksOrder
import com.habitrpg.android.habitica.models.user.*
import io.realm.Realm
import io.realm.RealmList
import java.lang.reflect.Type
import java.util.*

class UserDeserializer : JsonDeserializer<User> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
        val deserializeTrace = FirebasePerformance.getInstance().newTrace("UserDeserialize")
        deserializeTrace.start()
        val user = User()
        val obj = json.asJsonObject

        if (obj.has("_id")) {
            user.id = obj.get("_id").asString
        }

        if (obj.has("balance")) {
            user.balance = obj.get("balance").asDouble
        }
        if (obj.has("stats")) {
            user.stats = context.deserialize(obj.get("stats"), Stats::class.java)
        }
        if (obj.has("inbox")) {
            user.inbox = context.deserialize(obj.get("inbox"), Inbox::class.java)
        }
        if (obj.has("preferences")) {
            user.preferences = context.deserialize(obj.get("preferences"), Preferences::class.java)
        }
        if (obj.has("profile")) {
            user.profile = context.deserialize(obj.get("profile"), Profile::class.java)
        }
        if (obj.has("party")) {
            user.party = context.deserialize(obj.get("party"), UserParty::class.java)
            if (user.party != null && user.party?.quest != null) {
                user.party?.quest?.id = user.id
                if (!obj.getAsJsonObject("party").getAsJsonObject("quest").has("RSVPNeeded")) {
                    val realm = Realm.getDefaultInstance()
                    val quest = realm.where(Quest::class.java).equalTo("id", user.id).findFirst()
                    if (quest != null && quest.isValid) {
                        user.party?.quest?.RSVPNeeded = quest.RSVPNeeded
                    }
                }
            }
        }
        if (obj.has("purchased")) {
            user.purchased = context.deserialize(obj.get("purchased"), Purchases::class.java)
            if (obj.getAsJsonObject("purchased").has("plan")) {
                if (obj.getAsJsonObject("purchased").getAsJsonObject("plan").has("mysteryItems")) {
                    user.purchased?.plan?.mysteryItemCount = obj.getAsJsonObject("purchased").getAsJsonObject("plan").getAsJsonArray("mysteryItems").size()
                }
            }
        }
        if (obj.has("items")) {
            user.items = context.deserialize(obj.get("items"), Items::class.java)
            val item = OwnedItem()
            item.itemType = "special"
            item.key = "inventory_present"
            item.userID = user.id
            item.numberOwned = user.purchased?.plan?.mysteryItemCount ?: 0
            user.items?.special?.ownedItems = RealmList()
            user.items?.special?.ownedItems?.add(item)
        }
        if (obj.has("auth")) {
            user.authentication = context.deserialize(obj.get("auth"), Authentication::class.java)
        }
        if (obj.has("flags")) {
            user.flags = context.deserialize(obj.get("flags"), Flags::class.java)
        }
        if (obj.has("contributor")) {
            user.contributor = context.deserialize(obj.get("contributor"), ContributorInfo::class.java)
        }
        if (obj.has("invitations")) {
            user.invitations = context.deserialize(obj.get("invitations"), Invitations::class.java)
        }
        if (obj.has("tags")) {
            user.tags = context.deserialize(obj.get("tags"), object : TypeToken<RealmList<Tag>>() {

            }.type)
            for (tag in user.tags) {
                tag.userId = user.id
            }
        }
        if (obj.has("tasksOrder")) {
            user.tasksOrder = context.deserialize(obj.get("tasksOrder"), TasksOrder::class.java)
        }
        if (obj.has("challenges")) {
            user.challenges = RealmList()
            obj.getAsJsonArray("challenges").forEach {
                user.challenges?.add(ChallengeMembership(user.id ?: "", it.asString))
            }
        }

        if (obj.has("pushDevices")) {
            user.pushDevices = ArrayList()
            obj.getAsJsonArray("pushDevices")
                    .map { context.deserialize<PushDevice>(it, PushDevice::class.java) }
                    .forEach { (user.pushDevices as? ArrayList<PushDevice>)?.add(it) }
        }

        if (obj.has("lastCron")) {
            user.lastCron = context.deserialize(obj.get("lastCron"), Date::class.java)
        }

        if (obj.has("needsCron")) {
            user.needsCron = obj.get("needsCron").asBoolean
        }

        if (obj.has("achievements")) {
            val achievements = obj.getAsJsonObject("achievements")
            if (achievements.has("streak")) {
                try {
                    user.streakCount = obj.getAsJsonObject("achievements").get("streak").asInt
                } catch (ignored: UnsupportedOperationException) {
                }
            }
            if (achievements.has("quests")) {
                val questAchievements = RealmList<QuestAchievement>()
                for (entry in achievements.getAsJsonObject("quests").entrySet()) {
                    val questAchievement = QuestAchievement()
                    questAchievement.questKey = entry.key
                    questAchievement.count = entry.value.asInt
                    questAchievements.add(questAchievement)
                }
                user.questAchievements = questAchievements
            }
        }

        if (obj.has("_ABTests")) {
            user.abTests = RealmList()
            for (testJSON in obj.getAsJsonObject("_ABTests").entrySet()) {
                val test = ABTest()
                test.name = testJSON.key
                test.group = testJSON.value.asString
                user.abTests?.add(test)
            }
        }
        deserializeTrace.stop()
        return user
    }
}