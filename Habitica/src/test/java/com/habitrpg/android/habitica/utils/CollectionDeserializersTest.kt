package com.habitrpg.android.habitica.utils

import com.google.gson.reflect.TypeToken
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.SpriteSubstitutionContext
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.GroupAssignedDetails
import io.kotest.matchers.shouldBe
import io.realm.RealmList

class CollectionDeserializersTest :
    SerializerSpec({
        "SkillDeserializer" should {
            "flatten skills per class and keep the optional level" {
                val json = """{
                    "wizard": {"fireball": {"key": "fireball", "text": "Burst", "notes": "n", "target": "task", "mana": 10, "lvl": 11}},
                    "healer": {"heal": {"key": "heal", "text": "Heal", "notes": "n", "target": "self", "mana": 15}}
                }"""
                val skills: List<Skill> = gson.fromJson(json, object : TypeToken<MutableList<Skill>>() {}.type)
                skills.map { listOf(it.key, it.habitClass, it.mana, it.lvl) } shouldBe
                    listOf(listOf("fireball", "wizard", 10, 11), listOf("heal", "healer", 15, null))
            }
        }

        "AchievementListDeserializer" should {
            "flatten categories and default missing index and count to zero" {
                val json = """{
                    "basic": {"achievements": {
                        "streak": {"earned": true, "title": "Streak", "text": "t", "icon": "i", "index": 2, "optionalCount": 5},
                        "perfect": {"earned": false, "title": "Perfect", "text": "t", "icon": "i"}
                    }}
                }"""
                val achievements: List<Achievement> = gson.fromJson(json, object : TypeToken<MutableList<Achievement>>() {}.type)
                achievements.map { listOf(it.key, it.category, it.earned, it.index, it.optionalCount) } shouldBe
                    listOf(listOf("streak", "basic", true, 2, 5), listOf("perfect", "basic", false, 0, 0))
            }
        }

        "SpriteSubstitutionContextListDeserializer" should {
            "drop contexts without substitutions" {
                val json = """{"pets": {"Wolf": "Wolf-Spooky"}, "mounts": {}}"""
                val contexts: RealmList<SpriteSubstitutionContext> =
                    gson.fromJson(json, object : TypeToken<RealmList<SpriteSubstitutionContext>>() {}.type)
                contexts.single().key shouldBe "pets"
                contexts.single().substitutions["Wolf"] shouldBe "Wolf-Spooky"
            }
        }

        "TaskTagDeserializer" should {
            val type = object : TypeToken<RealmList<Tag>>() {}.type

            "deduplicate id entries and keep full tag objects" {
                val json = """["t1", "t1", {"id": "t2", "name": "Work"}, null]"""
                val tags: RealmList<Tag> = gson.fromJson(json, type)
                tags.map { it.id to it.name } shouldBe listOf("t1" to "", "t2" to "Work")
            }

            "return no tags for a non-array value" {
                gson.fromJson<RealmList<Tag>>("\"t1\"", type) shouldBe emptyList()
            }
        }

        "ChallengeListDeserializer" should {
            "accept both ids and challenge objects" {
                val json = """["c1", {"id": "c2", "name": "Name", "memberCount": 1, "prize": null, "official": false}]"""
                val challenges: List<Challenge> = gson.fromJson(json, object : TypeToken<MutableList<Challenge>>() {}.type)
                challenges.map { it.id to it.name } shouldBe listOf("c1" to null, "c2" to "Name")
            }
        }

        "EquipmentListDeserializer" should {
            "read equipment from an object map" {
                val json = """{"a": {"key": "weapon_1"}, "b": {"key": "weapon_2"}}"""
                val items: RealmList<Equipment> = gson.fromJson(json, object : TypeToken<RealmList<Equipment>>() {}.type)
                items.map { it.key } shouldBe listOf("weapon_1", "weapon_2")
            }

            "read equipment from an array" {
                val json = """[{"key": "weapon_1"}]"""
                val items: RealmList<Equipment> = gson.fromJson(json, object : TypeToken<RealmList<Equipment>>() {}.type)
                items.map { it.key } shouldBe listOf("weapon_1")
            }
        }

        "AssignedDetailsDeserializer" should {
            val type = object : TypeToken<RealmList<GroupAssignedDetails>>() {}.type

            "use object keys as the assigned user id" {
                val json = """{"user-1": {"assignedUsername": "one", "completed": true}}"""
                val details: RealmList<GroupAssignedDetails> = gson.fromJson(json, type)
                details.single().run {
                    assignedUserID shouldBe "user-1"
                    assignedUsername shouldBe "one"
                    completed shouldBe true
                }
            }

            "read an array as is" {
                val json = """[{"assignedUserID": "user-2"}]"""
                val details: RealmList<GroupAssignedDetails> = gson.fromJson(json, type)
                details.single().assignedUserID shouldBe "user-2"
            }

            "return an empty list for null input" {
                AssignedDetailsDeserializer().deserialize(null, type, null) shouldBe emptyList()
            }
        }
    })
