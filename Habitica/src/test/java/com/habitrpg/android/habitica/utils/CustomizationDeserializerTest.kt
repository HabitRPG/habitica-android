package com.habitrpg.android.habitica.utils

import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.inventory.Customization
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.realm.RealmList

class CustomizationDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()
        val listType = object : TypeToken<RealmList<Customization>>() {}.type

        "deserialize appearances" should {
            "read all types including hair categories and set info" {
                val json = """{
                    "shirt": {"black": {"price": 2, "set": {"key": "shirts", "setPrice": 5, "text": "Shirts", "availableFrom": "2015-01-01", "availableUntil": "2015-02-01"}}},
                    "skin": {"pale": {}},
                    "chair": {"none": {}},
                    "hair": {"color": {"red": {"price": 1}}}
                }"""
                val customizations: RealmList<Customization> = gson.fromJson(json, listType)
                customizations.map { listOf(it.type, it.category, it.identifier) } shouldBe
                    listOf(
                        listOf("shirt", null, "black"),
                        listOf("skin", null, "pale"),
                        listOf("chair", null, "none"),
                        listOf("hair", "color", "red"),
                    )
                customizations.first()!!.run {
                    price shouldBe 2
                    customizationSet shouldBe "shirts"
                    setPrice shouldBe 5
                    customizationSetName shouldBe "Shirts"
                    availableFrom.shouldNotBeNull()
                    availableUntil.shouldNotBeNull()
                }
            }
        }

        "deserialize backgrounds" should {
            "name monthly sets and special sets" {
                val json = """{
                    "backgrounds092015": {"beach": {"text": "Beach", "notes": "Sand"}},
                    "eventBackgrounds": {"party": {"text": "Party", "notes": ""}},
                    "incentiveBackgrounds": {"blue": {"text": "Blue", "notes": ""}},
                    "timeTravelBackgrounds": {"steam": {"text": "Steam", "notes": ""}}
                }"""
                val backgrounds: RealmList<Customization> = gson.fromJson(json, listType)
                backgrounds[0]!!.run {
                    type shouldBe "background"
                    identifier shouldBe "beach"
                    text shouldBe "Beach"
                    notes shouldBe "Sand"
                    customizationSet shouldBe "2015.09"
                    customizationSetName!! shouldStartWith "SET 1: "
                    customizationSetName!! shouldEndWith " 2015"
                    price shouldBe 7
                    setPrice shouldBe 15
                }
                backgrounds.drop(1).map { listOf(it.customizationSetName, it.price, it.isBuyable) } shouldBe
                    listOf(
                        listOf("EVENT BACKGROUNDS", 0, false),
                        listOf("PLAIN BACKGROUND SET", 0, false),
                        listOf("STEAMPUNK BACKGROUNDS", 1, false),
                    )
            }
        }
    })
