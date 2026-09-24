package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.user.User
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.Date

class UserDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        beforeSpec {
            mockkStatic(FirebasePerformance::class)
            every { FirebasePerformance.getInstance() } returns mockk(relaxed = true)
        }
        afterSpec { unmockkStatic(FirebasePerformance::class) }

        "deserialize" should {
            "read the user and its nested objects" {
                val json = """{
                    "_id": "user-1", "_v": 12, "balance": 2.5,
                    "stats": {"lvl": 20}, "profile": {"name": "User"},
                    "party": {"_id": "party-1", "quest": {"key": "quest-1", "completed": "quest-0"}},
                    "purchased": {"plan": {"customerId": "c", "mysteryItems": ["a", "b"]}},
                    "items": {"eggs": {"Wolf": 2}, "special": {"snowball": 1}},
                    "auth": {"local": {"username": "user"}},
                    "contributor": {"level": 1},
                    "tags": [{"id": "tag-1", "name": "Work"}, "tag-2"],
                    "achievements": {
                        "partyUp": true, "streak": 7, "rebirths": 2, "rebirthLevel": 50,
                        "quests": {"dilatory": 3}, "challenges": ["Won"]
                    },
                    "challenges": ["c1", "c2"], "_ABTests": {"subscriptionPosition": "top"},
                    "lastCron": "2015-09-28T13:00:00.000Z", "needsCron": true
                }"""
                val user = gson.fromJson(json, User::class.java)
                user.id shouldBe "user-1"
                user.versionNumber shouldBe 12
                user.balance shouldBe 2.5
                user.stats?.lvl shouldBe 20
                user.profile?.name shouldBe "User"
                user.party?.quest?.id shouldBe "user-1"
                user.party?.quest?.completed shouldBe "quest-0"
                user.purchased?.plan?.mysteryItemCount shouldBe 2
                user.items?.special?.map { Triple(it.key, it.numberOwned, it.itemType) } shouldBe
                    listOf(Triple("snowball", 1, "special"), Triple("inventory_present", 2, "special"))
                user.items?.eggs?.map { it.itemType } shouldBe listOf("eggs")
                user.authentication?.localAuthentication?.username shouldBe "user"
                user.contributor?.level shouldBe 1
                user.tags.map { it.id to it.userId } shouldBe listOf("tag-1" to "user-1")
                user.achievements.map { it.key to it.earned } shouldBe
                    listOf("partyUp" to true, "streak" to false, "rebirths" to false, "rebirthLevel" to false)
                user.streakCount shouldBe 7
                user.rebirths shouldBe 2
                user.rebirthLevel shouldBe 50
                user.questAchievements.map { it.questKey to it.count } shouldBe listOf("dilatory" to 3)
                user.challengeAchievements shouldBe listOf("Won")
                user.lastCron shouldBe Date(1443445200000)
                user.needsCron shouldBe true
                user.challenges?.map { it.userID to it.challengeID } shouldBe listOf("user-1" to "c1", "user-1" to "c2")
                user.abTests?.map { it.name to it.group } shouldBe listOf("subscriptionPosition" to "top")
            }

            "ignore a null completed quest" {
                val json = """{"_id": "user-1", "party": {"quest": {"key": "quest-1", "completed": null}}}"""
                gson.fromJson(json, User::class.java).party?.quest?.completed shouldBe null
            }
        }
    })
