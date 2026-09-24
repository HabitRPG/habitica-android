package com.habitrpg.android.habitica.utils

import com.google.gson.Gson
import com.habitrpg.android.habitica.models.inventory.Quest
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class QuestDeserializerTest :
    WordSpec({
        val gson = Gson().newBuilder().registerTypeAdapter(Quest::class.java, QuestDeserializer()).create()

        "deserialize" should {
            "mark rsvpNeededWasSpecified false and leave rsvpNeeded at its default when RSVPNeeded is absent" {
                val json = """{"key": "quest-1", "active": true}"""
                val quest = gson.fromJson(json, Quest::class.java)
                quest.rsvpNeededWasSpecified shouldBe false
                quest.rsvpNeeded shouldBe false
            }

            "mark rsvpNeededWasSpecified true and use the given value when RSVPNeeded is present" {
                val json = """{"key": "quest-1", "active": true, "RSVPNeeded": true}"""
                val quest = gson.fromJson(json, Quest::class.java)
                quest.rsvpNeededWasSpecified shouldBe true
                quest.rsvpNeeded shouldBe true
            }

            "read progress, leader and member participation" {
                val json = """{
                    "key": "quest-1", "leader": "user-1",
                    "progress": {"hp": 50.5, "rage": 3, "up": 1.5, "down": 0.5, "collectedItems": 4, "collect": {"soap": 2}},
                    "members": {"user-1": true, "user-2": null}
                }"""
                val quest = gson.fromJson(json, Quest::class.java)
                quest.leader shouldBe "user-1"
                quest.progress?.hp shouldBe 50.5
                quest.progress?.rage shouldBe 3.0
                quest.progress?.up shouldBe 1.5f
                quest.progress?.down shouldBe 0.5f
                quest.progress?.collectedItems shouldBe 4
                quest.progress?.collect?.map { it.key to it.count } shouldBe listOf("soap" to 2)
                quest.members?.map { it.key to it.isParticipating } shouldBe listOf("user-1" to true, "user-2" to null)
            }

            "stop after progress when the key is null" {
                val json = """{"key": null, "active": true, "progress": {"hp": 1}}"""
                val quest = gson.fromJson(json, Quest::class.java)
                quest.progress?.hp shouldBe 1.0
                quest.active shouldBe false
            }
        }
    })
