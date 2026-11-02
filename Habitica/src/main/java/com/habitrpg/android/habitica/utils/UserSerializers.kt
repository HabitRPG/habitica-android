package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.QuestAchievement
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.models.user.ABTest
import com.habitrpg.android.habitica.models.user.OwnedCustomization
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.Purchases
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.UserAchievement
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication
import com.habitrpg.shared.habitica.serialization.JsonTreeSerializer
import com.habitrpg.shared.habitica.serialization.array
import com.habitrpg.shared.habitica.serialization.bool
import com.habitrpg.shared.habitica.serialization.decode
import com.habitrpg.shared.habitica.serialization.double
import com.habitrpg.shared.habitica.serialization.int
import com.habitrpg.shared.habitica.serialization.obj
import com.habitrpg.shared.habitica.serialization.string
import io.realm.RealmList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

object UserSerializer : JsonTreeSerializer<User>("User") {
    override fun Json.read(element: JsonElement): User {
        val obj = element as JsonObject
        val user = User()
        if (obj.containsKey("_id")) user.id = obj.string("_id") ?: ""
        obj.int("_v")?.let { user.versionNumber = it }
        obj.double("balance")?.let { user.balance = it }
        user.stats = decode(obj["stats"])
        user.inbox = decode(obj["inbox"])
        user.permissions = decode(obj["permissions"])
        user.preferences = decode(obj["preferences"])
        user.profile = decode(obj["profile"])
        obj.obj("party")?.let { party ->
            user.party = decode(party)
            user.party?.quest?.let { quest ->
                quest.id = user.id
                party.obj("quest")?.string("completed")?.let { quest.completed = it }
            }
        }
        obj.obj("purchased")?.let { purchased ->
            user.purchased = decode(purchased)
            purchased.obj("plan")?.array("mysteryItems")?.let { user.purchased?.plan?.mysteryItemCount = it.size }
        }
        obj.obj("items")?.let { items ->
            user.items = decode(items)
            user.items?.special?.add(
                OwnedItem().apply {
                    itemType = "special"
                    key = "inventory_present"
                    userID = user.id
                    numberOwned = user.purchased?.plan?.mysteryItemCount ?: 0
                },
            )
            user.items?.setItemTypes()
        }
        user.authentication = decode(obj["auth"])
        user.flags = decode(obj["flags"])
        user.contributor = decode(obj["contributor"])
        user.backer = decode(obj["backer"])
        user.invitations = decode(obj["invitations"])
        obj.array("tags")?.forEach { entry ->
            if (entry !is JsonObject) return@forEach
            decode<Tag>(entry)?.let {
                it.userId = user.id
                user.tags.add(it)
            }
        }
        obj.obj("achievements")?.let { achievements ->
            achievements.forEach { (key, value) ->
                val primitive = value as? JsonPrimitive ?: return@forEach
                user.achievements.add(UserAchievement(key, primitive.booleanOrNull ?: false))
            }
            achievements.int("streak")?.let { user.streakCount = it }
            achievements.obj("quests")?.forEach { (key, value) ->
                user.questAchievements.add(
                    QuestAchievement().apply {
                        questKey = key
                        count = (value as? JsonPrimitive)?.doubleOrNull?.toInt() ?: 0
                    },
                )
            }
            achievements.array("challenges")?.forEach { user.challengeAchievements.add((it as JsonPrimitive).content) }
            achievements.int("rebirths")?.let { user.rebirths = it }
            achievements.int("rebirthLevel")?.let { user.rebirthLevel = it }
        }
        user.tasksOrder = decode(obj["tasksOrder"])
        obj.array("challenges")?.let { challenges ->
            user.challenges = RealmList()
            challenges.forEach { user.challenges?.add(ChallengeMembership(user.id ?: "", (it as JsonPrimitive).content)) }
        }
        user.pushDevices = decode(obj["pushDevices"])
        user.lastCron = decode(obj["lastCron"])
        obj.bool("needsCron")?.let { user.needsCron = it }
        obj.obj("_ABTests")?.let { tests ->
            user.abTests = RealmList()
            tests.forEach { (key, value) ->
                user.abTests?.add(
                    ABTest().apply {
                        name = key
                        group = (value as JsonPrimitive).content
                    },
                )
            }
        }
        return user
    }
}

/** Purchased customizations come as nested maps of key to true or false. */
object PurchasesSerializer : JsonTreeSerializer<Purchases>("Purchases") {
    override fun Json.read(element: JsonElement): Purchases {
        val obj = element as JsonObject
        val purchases = Purchases()

        fun add(
            type: String,
            category: String?,
            values: JsonObject,
        ) = values.forEach { (key, value) ->
            purchases.customizations?.add(
                OwnedCustomization().apply {
                    this.key = key
                    this.type = type
                    category?.let { this.category = it }
                    purchased = (value as? JsonPrimitive)?.booleanOrNull ?: false
                },
            )
        }
        for (type in listOf("background", "shirt", "skin")) {
            obj.obj(type)?.let { add(type, null, it) }
        }
        obj.obj("hair")?.forEach { (category, values) -> add("hair", category, values as JsonObject) }
        purchases.plan = decode(obj["plan"])
        return purchases
    }
}

/** Emails are either plain strings or objects with a `value`. */
object SocialAuthenticationSerializer : JsonTreeSerializer<SocialAuthentication>("SocialAuthentication") {
    override fun Json.read(element: JsonElement): SocialAuthentication {
        val authentication = SocialAuthentication()
        (element as? JsonObject)?.array("emails")?.forEach { entry ->
            when (entry) {
                is JsonObject -> authentication.emails.add(entry.string("value") ?: "")
                is JsonPrimitive -> authentication.emails.add(entry.content)
                else -> {}
            }
        }
        return authentication
    }
}
