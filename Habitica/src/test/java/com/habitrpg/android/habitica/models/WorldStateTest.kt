package com.habitrpg.android.habitica.models

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class WorldStateTest :
    WordSpec({
        "findNpcImageSuffix" should {
            "returns if suffix is set directly" {
                val worldState = WorldState()
                worldState.npcImageSuffix = "test"
                worldState.currentEvent = WorldStateEvent()
                worldState.currentEvent?.npcImageSuffix = "test2"
                worldState.findNpcImageSuffix() shouldBe "test"
            }

            "returns if suffix is set in current event" {
                val worldState = WorldState()
                worldState.currentEvent = WorldStateEvent()
                worldState.currentEvent?.npcImageSuffix = "test"
                worldState.events.add(WorldStateEvent())
                worldState.events.first()?.npcImageSuffix = "test2"
                worldState.findNpcImageSuffix() shouldBe "test"
            }

            "returns if suffix is set in event list" {
                val worldState = WorldState()
                worldState.events.add(WorldStateEvent())
                worldState.events.first()?.npcImageSuffix = "test"
                worldState.findNpcImageSuffix() shouldBe "test"
            }
        }

        "getCurrentSeason" should {
            "returns if multiple events exist" {
                val worldState = WorldState()
                worldState.events.add(WorldStateEvent())
                worldState.events.add(WorldStateEvent())
                worldState.events.last()?.season = "test"
                worldState.getCurrentSeason() shouldBe "test"
            }

            "returns if season is set in current event" {
                val worldState = WorldState()
                worldState.currentEvent = WorldStateEvent()
                worldState.currentEvent?.season = "test"
                worldState.events.add(WorldStateEvent())
            }
        }
    })
