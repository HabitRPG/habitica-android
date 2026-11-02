package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.tasks.TaskList
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class ListSerializersTest :
    SerializerSpec({
        "AchievementListSerializer" should {
            "flatten categories and default missing index and count to zero" {
                val json = """{
                    "basic": {"achievements": {
                        "streak": {"earned": true, "title": "Streak", "text": "t", "icon": "i", "index": 2, "optionalCount": 5},
                        "perfect": {"earned": false, "title": "Perfect", "text": "t", "icon": "i"}
                    }}
                }"""
                val achievements = parse<List<Achievement>>(json)
                achievements.map { listOf(it.key, it.category, it.earned, it.index, it.optionalCount) } shouldBe
                    listOf(listOf("streak", "basic", true, 2, 5), listOf("perfect", "basic", false, 0, 0))
            }
        }

        "SpriteSubstitutionListSerializer" should {
            "drop contexts without substitutions" {
                val contexts = parse(SpriteSubstitutionListSerializer, """{"pets": {"Wolf": "Wolf-Spooky"}, "mounts": {}}""")
                contexts.single().key shouldBe "pets"
                contexts.single().substitutions["Wolf"] shouldBe "Wolf-Spooky"
            }
        }

        "TaskListSerializer" should {
            "deduplicate tag ids and keep full tag objects" {
                val json = """[{"_id": "task-1", "type": "todo", "tags": ["t1", "t1", {"id": "t2", "name": "Work"}, null]}]"""
                val task = parse<TaskList>(json).tasks.getValue("task-1")
                task.tags?.map { it.id to it.name } shouldBe listOf("t1" to "", "t2" to "Work")
            }
        }

        "EquipmentListSerializer" should {
            "read equipment from an object map" {
                val items = parse(EquipmentListSerializer, """{"a": {"key": "weapon_1"}, "b": {"key": "weapon_2"}}""")
                items.map { it.key } shouldBe listOf("weapon_1", "weapon_2")
            }

            "read equipment from an array" {
                parse(EquipmentListSerializer, """[{"key": "weapon_1"}]""").map { it.key } shouldBe listOf("weapon_1")
            }
        }

        "AssignedDetailsListSerializer" should {
            "use object keys as the assigned user id" {
                val details = parse(AssignedDetailsListSerializer, """{"user-1": {"assignedUsername": "one", "completed": true}}""")
                details.single().run {
                    assignedUserID shouldBe "user-1"
                    assignedUsername shouldBe "one"
                    completed shouldBe true
                }
            }

            "read an array as is" {
                parse(AssignedDetailsListSerializer, """[{"assignedUserID": "user-2"}]""").single().assignedUserID shouldBe "user-2"
            }

            "return an empty list for null input" {
                parse(AssignedDetailsListSerializer, "null").shouldBeEmpty()
            }
        }

        "OwnedItemListSerializer" should {
            "map primitive counts and skip non-primitive entries" {
                val items = parse(OwnedItemListSerializer, """{"Wolf": 3, "Fox": 0, "nested": {"a": 1}}""")
                items.map { it.key to it.numberOwned } shouldBe listOf("Wolf" to 3, "Fox" to 0)
            }

            "return an empty list for null input" {
                parse(OwnedItemListSerializer, "null").shouldBeEmpty()
            }
        }

        "OwnedPetListSerializer" should {
            "map trained values" {
                val pets = parse(OwnedPetListSerializer, """{"Wolf-Base": 5, "Fox-Red": -1}""")
                pets.map { it.key to it.trained } shouldBe listOf("Wolf-Base" to 5, "Fox-Red" to -1)
            }
        }

        "OwnedMountListSerializer" should {
            "treat null values as not owned" {
                val mounts = parse(OwnedMountListSerializer, """{"Wolf-Base": true, "Fox-Red": null, "Cat-Blue": false}""")
                mounts.map { it.key to it.owned } shouldBe listOf("Wolf-Base" to true, "Fox-Red" to false, "Cat-Blue" to false)
            }
        }

        "OwnedEquipmentListSerializer" should {
            "map primitive entries and skip objects" {
                val equipment = parse(OwnedEquipmentListSerializer, """{"weapon_warrior_1": true, "armor_base_0": false, "meta": {}}""")
                equipment.map { it.key to it.owned } shouldBe listOf("weapon_warrior_1" to true, "armor_base_0" to false)
            }
        }
    })
