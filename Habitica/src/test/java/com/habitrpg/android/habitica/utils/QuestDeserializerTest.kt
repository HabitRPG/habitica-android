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
        }
    })
