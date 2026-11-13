package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.WorldState
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class WorldStateSerializationTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read the world boss, rage strikes and events" {
                val json = """{
                    "worldBoss": {
                        "active": true, "key": "dysheartener",
                        "progress": {"hp": 100.5, "rage": 20},
                        "extra": {"worldDmg": {"tavern": true, "market": false}}
                    },
                    "npcImageSuffix": "_spring",
                    "currentEvent": {"event": "spring", "season": "spring"},
                    "currentEventList": [{"event": "spring"}, {"event": "gems"}]
                }"""
                val state = gson.fromJson(json, WorldState::class.java)
                state.worldBossActive shouldBe true
                state.worldBossKey shouldBe "dysheartener"
                state.progress?.hp shouldBe 100.5
                state.progress?.rage shouldBe 20.0
                state.rageStrikes?.map { it.key to it.wasHit } shouldBe listOf("tavern" to true, "market" to false)
                state.npcImageSuffix shouldBe "_spring"
                state.currentEvent?.eventKey shouldBe "spring"
                state.events.map { it.eventKey } shouldBe listOf("spring", "gems")
            }

            "ignore null boss values and events without a current event" {
                val json = """{"worldBoss": {"active": null, "key": null}, "currentEvent": null, "currentEventList": [{"event": "x"}]}"""
                val state = gson.fromJson(json, WorldState::class.java)
                state.worldBossActive shouldBe false
                state.worldBossKey shouldBe ""
                state.progress.shouldBeNull()
                state.currentEvent.shouldBeNull()
                state.events shouldBe emptyList()
            }

            "return a default state for null input" {
                WorldStateSerialization().deserialize(null, null, null).worldBossKey shouldBe ""
            }
        }
    })
