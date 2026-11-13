package com.habitrpg.android.habitica.utils

import com.google.gson.JsonParser
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class OwnedListDeserializersTest :
    SerializerSpec({
        "OwnedItemListDeserializer" should {
            "map primitive counts and skip non-primitive entries" {
                val json = JsonParser.parseString("""{"Wolf": 3, "Fox": 0, "nested": {"a": 1}}""")
                val items = OwnedItemListDeserializer().deserialize(json, null, deserializationContext)
                items.map { it.key to it.numberOwned } shouldBe listOf("Wolf" to 3, "Fox" to 0)
            }

            "return an empty list for null input" {
                OwnedItemListDeserializer().deserialize(null, null, null).shouldBeEmpty()
            }
        }

        "OwnedPetListDeserializer" should {
            "map trained values" {
                val json = JsonParser.parseString("""{"Wolf-Base": 5, "Fox-Red": -1}""")
                val pets = OwnedPetListDeserializer().deserialize(json, null, deserializationContext)
                pets.map { it.key to it.trained } shouldBe listOf("Wolf-Base" to 5, "Fox-Red" to -1)
            }

            "return an empty list for null input" {
                OwnedPetListDeserializer().deserialize(null, null, null).shouldBeEmpty()
            }
        }

        "OwnedMountListDeserializer" should {
            "treat null values as not owned" {
                val json = JsonParser.parseString("""{"Wolf-Base": true, "Fox-Red": null, "Cat-Blue": false}""")
                val mounts = OwnedMountListDeserializer().deserialize(json, null, deserializationContext)
                mounts.map { it.key to it.owned } shouldBe
                    listOf("Wolf-Base" to true, "Fox-Red" to false, "Cat-Blue" to false)
            }

            "return an empty list for null input" {
                OwnedMountListDeserializer().deserialize(null, null, null).shouldBeEmpty()
            }
        }

        "OwnedEquipmentListDeserializer" should {
            "map primitive entries and skip objects" {
                val json = JsonParser.parseString("""{"weapon_warrior_1": true, "armor_base_0": false, "meta": {}}""")
                val equipment = OwnedEquipmentListDeserializer().deserialize(json, null, deserializationContext)
                equipment.map { it?.key to it?.owned } shouldBe
                    listOf("weapon_warrior_1" to true, "armor_base_0" to false)
            }
        }
    })
