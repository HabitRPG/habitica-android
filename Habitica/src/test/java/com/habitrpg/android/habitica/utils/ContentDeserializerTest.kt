package com.habitrpg.android.habitica.utils

import com.google.firebase.perf.FirebasePerformance
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.ContentResult
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

class ContentDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        beforeSpec {
            mockkStatic(FirebasePerformance::class)
            every { FirebasePerformance.getInstance() } returns mockk(relaxed = true)
        }
        afterSpec { unmockkStatic(FirebasePerformance::class) }

        "deserialize" should {
            "read items, animals, spells and extras" {
                val json = """{
                    "potion": {"key": "HealthPotion"}, "armoire": {"key": "Armoire"}, "gear": {},
                    "quests": {"dilatory": {"key": "dilatory"}},
                    "eggs": {"Wolf": {"key": "Wolf", "text": "Wolf", "mountText": "Wolf Steed"}},
                    "food": {"Meat": {"key": "Meat"}},
                    "hatchingPotions": {"Red": {"key": "Red", "text": "Red"}},
                    "petInfo": {
                        "Wolf-Red": {"key": "Wolf-Red", "egg": "Wolf", "potion": "Red", "type": "premium"},
                        "Dragon-Gold": {"key": "Dragon-Gold", "egg": "Dragon", "potion": "Gold", "text": "Golden Dragon", "type": "special"}
                    },
                    "mountInfo": {"Wolf-Red": {"key": "Wolf-Red", "egg": "Wolf", "potion": "Red", "type": "premium"}},
                    "spells": {"wizard": {"fireball": {"key": "fireball", "text": "Burst", "notes": "n", "target": "task", "mana": 10, "lvl": 11}}},
                    "categoryOptions": [{"key": "hobbies", "label": "Hobbies"}],
                    "special": {"snowball": {"key": "snowball"}},
                    "mystery": {"201501": {"key": "201501"}},
                    "appearances": {}, "backgrounds": {},
                    "faq": {"questions": [{"question": "Q", "web": "A"}]}
                }"""
                val content = gson.fromJson(json, ContentResult::class.java)
                content.potion?.key shouldBe "HealthPotion"
                content.armoire?.key shouldBe "Armoire"
                content.quests.map { it.key } shouldBe listOf("dilatory")
                content.eggs.map { it.key } shouldBe listOf("Wolf")
                content.food.map { it.key } shouldBe listOf("Meat")
                content.hatchingPotions.map { it.key } shouldBe listOf("Red")
                content.pets.map { Triple(it.key, it.text, it.premium) } shouldBe
                    listOf(Triple("Wolf-Red", "Red Wolf", true), Triple("Dragon-Gold", "Golden Dragon", false))
                content.mounts.map { Triple(it.key, it.text, it.premium) } shouldBe listOf(Triple("Wolf-Red", "Red Wolf Steed", true))
                content.spells.map { Triple(it.key, it.habitClass, it.lvl) } shouldBe listOf(Triple("fireball", "wizard", 11))
                content.categoryOptions.map { it.key to it.label } shouldBe listOf("hobbies" to "Hobbies")
                content.special.map { it.key } shouldBe listOf("snowball")
                content.mystery.map { it.key } shouldBe listOf("201501")
                content.backgrounds.map { it.customizationSet to it.identifier } shouldBe listOf("incentiveBackgrounds" to "")
                content.faq.map { it.answer } shouldBe listOf("A")
            }
        }
    })
