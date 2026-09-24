package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.members.Member
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class MemberSerializationTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read nested objects and outfit details" {
                val json = """{
                    "_id": "member-1",
                    "stats": {"lvl": 12},
                    "profile": {"name": "Member"},
                    "party": {"_id": "party-1", "quest": {"key": "quest-1"}},
                    "items": {
                        "currentMount": "Wolf-Base", "currentPet": "Fox-Red",
                        "gear": {"costume": {"head": "head_1"}, "equipped": {"weapon": "weapon_1"}}
                    },
                    "contributor": {"level": 2}, "backer": {"tier": 1},
                    "auth": {"local": {"username": "member"}},
                    "loginIncentives": 7
                }"""
                val member = gson.fromJson(json, Member::class.java)
                member.id shouldBe "member-1"
                member.stats?.lvl shouldBe 12
                member.profile?.name shouldBe "Member"
                member.party?.quest?.id shouldBe "member-1"
                member.currentMount shouldBe "Wolf-Base"
                member.currentPet shouldBe "Fox-Red"
                member.costume?.head shouldBe "head_1"
                member.equipped?.weapon shouldBe "weapon_1"
                member.items?.gear.shouldBeNull()
                member.contributor?.level shouldBe 2
                member.backer?.tier shouldBe 1
                member.authentication?.localAuthentication?.username shouldBe "member"
                member.loginIncentives shouldBe 7
            }

            "skip null current mount and pet" {
                val json = """{"_id": "member-1", "items": {"currentMount": null, "currentPet": null}}"""
                val member = gson.fromJson(json, Member::class.java)
                member.currentMount.shouldBeNull()
                member.currentPet.shouldBeNull()
            }
        }
    })
