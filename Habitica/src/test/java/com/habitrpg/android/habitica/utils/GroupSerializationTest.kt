package com.habitrpg.android.habitica.utils

import com.google.gson.JsonObject
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.social.Group
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class GroupSerializationTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read group fields, managers, quest participants and rage strikes" {
                val json = """{
                    "_id": "group-1", "name": "Party", "description": "Desc", "summary": "Sum",
                    "leaderMessage": "Hi", "privacy": "private", "memberCount": 3, "balance": 1.5,
                    "logo": "logo.png", "type": "party",
                    "leader": {"_id": "leader-1", "profile": {"name": "Leader"}},
                    "managers": {"m1": true, "m2": false},
                    "quest": {
                        "key": "quest-1", "active": true,
                        "members": {"p1": true, "p2": null},
                        "extra": {"worldDmg": {"tavern": true, "stable": false}}
                    },
                    "leaderOnly": {"challenges": true, "getGems": false},
                    "categories": [{"slug": "hobbies", "name": "Hobbies"}]
                }"""
                val group = gson.fromJson(json, Group::class.java)
                group.id shouldBe "group-1"
                group.name shouldBe "Party"
                group.description shouldBe "Desc"
                group.summary shouldBe "Sum"
                group.leaderMessage shouldBe "Hi"
                group.privacy shouldBe "private"
                group.memberCount shouldBe 3
                group.balance shouldBe 1.5
                group.logo shouldBe "logo.png"
                group.type shouldBe "party"
                group.leaderID shouldBe "leader-1"
                group.leaderName shouldBe "Leader"
                group.managers shouldBe listOf("m1")
                group.quest?.id shouldBe "group-1"
                group.quest?.participants?.map { it.id to it.participatesInQuest } shouldBe
                    listOf("p1" to true, "p2" to null)
                group.quest?.rageStrikes?.map { it.key to it.wasHit } shouldBe
                    listOf("tavern" to true, "stable" to false)
                group.leaderOnlyChallenges shouldBe true
                group.leaderOnlyGetGems shouldBe false
                group.categories?.map { it.slug } shouldBe listOf("hobbies")
            }

            "accept a plain leader id and null optional strings" {
                val json = """{"_id": "group-1", "name": "Guild", "leader": "leader-1", "description": null, "logo": null}"""
                val group = gson.fromJson(json, Group::class.java)
                group.leaderID shouldBe "leader-1"
                group.leaderName.shouldBeNull()
                group.description.shouldBeNull()
                group.logo.shouldBeNull()
            }
        }

        "serialize" should {
            "write editable fields and leaderOnly flags" {
                val group =
                    Group().apply {
                        name = "Guild"
                        description = "Desc"
                        leaderID = "leader-1"
                        type = "guild"
                        leaderOnlyChallenges = true
                    }
                val obj = gson.toJsonTree(group) as JsonObject
                obj["name"].asString shouldBe "Guild"
                obj["description"].asString shouldBe "Desc"
                obj["leader"].asString shouldBe "leader-1"
                obj["type"].asString shouldBe "guild"
                obj["leaderOnly"].asJsonObject["challenges"].asBoolean shouldBe true
                obj["leaderOnly"].asJsonObject["getGems"].asBoolean shouldBe false
                obj.has("_id") shouldBe false
            }
        }
    })
