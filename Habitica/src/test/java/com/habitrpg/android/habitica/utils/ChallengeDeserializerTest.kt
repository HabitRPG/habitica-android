package com.habitrpg.android.habitica.utils

import com.google.gson.JsonObject
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.social.Challenge
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.Date

class ChallengeDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read challenge, leader, group and categories" {
                val json = """{
                    "id": "c1", "name": "Challenge", "shortName": "C", "description": "Desc", "summary": "Sum",
                    "memberCount": 5, "prize": 10, "official": true,
                    "leader": {"_id": "leader-1", "profile": {"name": "Leader"}},
                    "group": {"_id": "group-1", "name": "Guild"},
                    "categories": [{"_id": "cat-1", "slug": "hobbies", "name": "Hobbies"}],
                    "createdAt": "2015-09-28T13:00:00.000Z"
                }"""
                val challenge = gson.fromJson(json, Challenge::class.java)
                challenge.id shouldBe "c1"
                challenge.name shouldBe "Challenge"
                challenge.shortName shouldBe "C"
                challenge.description shouldBe "Desc"
                challenge.summary shouldBe "Sum"
                challenge.memberCount shouldBe 5
                challenge.prize shouldBe 10
                challenge.official shouldBe true
                challenge.leaderId shouldBe "leader-1"
                challenge.leaderName shouldBe "Leader"
                challenge.groupId shouldBe "group-1"
                challenge.groupName shouldBe "Guild"
                challenge.categories.map { Triple(it.id, it.slug, it.name) } shouldBe listOf(Triple("cat-1", "hobbies", "Hobbies"))
                challenge.createdAt shouldBe Date(1443445200000)
            }

            "prefer the leader id field and tolerate null prize, leader and group" {
                val json = """{"id": "c1", "name": "C", "memberCount": 0, "prize": null, "official": false, "leader": {"id": "l1", "_id": "l2", "profile": {"name": "L"}}, "group": null}"""
                val challenge = gson.fromJson(json, Challenge::class.java)
                challenge.leaderId shouldBe "l1"
                challenge.prize shouldBe 0
                challenge.groupId.shouldBeNull()
            }
        }

        "serialize" should {
            "write the public fields and the group id" {
                val challenge =
                    Challenge().apply {
                        id = "c1"
                        name = "Challenge"
                        prize = 4
                        groupId = "group-1"
                    }
                val obj = gson.toJsonTree(challenge) as JsonObject
                obj["id"].asString shouldBe "c1"
                obj["name"].asString shouldBe "Challenge"
                obj["prize"].asInt shouldBe 4
                obj["group"].asString shouldBe "group-1"
                obj["tasksOrder"].isJsonNull shouldBe true
            }
        }
    })
