package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import com.habitrpg.shared.habitica.models.responses.TaskDirectionDataDrop
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException

class SimpleDeserializersTest :
    SerializerSpec({

        "FeedResponseDeserializer" should {
            "read the raw integer value" {
                parse<FeedResponse>("42").value shouldBe 42
            }
        }

        "InviteResponseDeserializer" should {
            "parse a string as an email invite" {
                parse<InviteResponse>("\"a@b.c\"") shouldBe InviteResponse.EmailInvite("a@b.c")
            }

            "parse an object as a user invite" {
                val json = """{"id": "u1", "name": "Name", "inviter": "u2"}"""
                parse<InviteResponse>(json) shouldBe InviteResponse.UserInvite("u1", "Name", "u2")
            }

            "reject other shapes" {
                shouldThrow<SerializationException> { parse<InviteResponse>("12") }
            }
        }

        "SocialAuthenticationDeserializer" should {
            "collect emails given as strings or value objects" {
                val json = """{"emails": ["a@b.c", {"value": "d@e.f"}, 5]}"""
                parse<SocialAuthentication>(json).emails shouldBe listOf("a@b.c", "d@e.f", "5")
            }

            "ignore a non-array emails field" {
                parse<SocialAuthentication>("""{"emails": "a@b.c"}""").emails shouldBe emptyList()
            }
        }

        "FindUsernameResultDeserializer" should {
            "read contributor and auth" {
                val json = """{"contributor": {"level": 3}, "auth": {"local": {"username": "tester"}}}"""
                val result = parse<FindUsernameResult>(json)
                result.contributor?.level shouldBe 3
                result.formattedUsername shouldBe "@tester"
            }

            "leave fields empty when absent" {
                val result = parse<FindUsernameResult>("{}")
                result.contributor.shouldBeNull()
                result.authentication.shouldBeNull()
            }
        }

        "QuestCollectDeserializer" should {
            "map collect entries by key" {
                val json = """{"soapBars": {"count": 20, "text": "Bars of Soap"}}"""
                val items = parse(QuestCollectListSerializer, json)
                items.single().run {
                    key shouldBe "soapBars"
                    count shouldBe 20
                    text shouldBe "Bars of Soap"
                }
            }
        }

        "QuestDropItemsListSerialization" should {
            "merge duplicate keys into a count" {
                val json = """[{"key": "Egg", "type": "eggs"}, {"key": "Egg", "type": "eggs"}, {"key": "Gold", "type": "gp"}]"""
                val items = parse(QuestDropItemListSerializer, json)
                items.map { it.key to it.count } shouldBe listOf("Egg" to 2, "Gold" to 1)
            }
        }

        "TutorialStepListDeserializer" should {
            "read common and android groups in order" {
                val json = """{"common": {"habits": true}, "android": {"party": false}, "ios": {"x": true}}"""
                val steps = parse(TutorialStepListSerializer, json)
                steps.map { Triple(it.tutorialGroup, it.identifier, it.wasCompleted) } shouldBe
                    listOf(Triple("common", "habits", true), Triple("android", "party", false))
            }
        }

        "FAQArticleListDeserilializer" should {
            "prefer the android answer and fall back to web" {
                val json = """{"questions": [
                    {"question": "Q1", "android": "A1", "web": "W1"},
                    {"question": "Q2", "web": "W2"}
                ]}"""
                val articles = parse<ContentResult>("""{"faq": $json}""").faq
                articles.map { Triple(it.position, it.question, it.answer) } shouldBe
                    listOf(Triple(0, "Q1", "A1"), Triple(1, "Q2", "W2"))
            }
        }

        "TaskDirectionData" should {
            "read a drop that only has some fields" {
                val json = """{"delta": 1.5, "hp": 50, "lvl": 3, "_tmp": {"drop": {"key": "Wolf", "type": "Egg"}}}"""
                val data = parse<TaskDirectionData>(json)
                data.delta shouldBe 1.5f
                data._tmp?.drop shouldBe TaskDirectionDataDrop(key = "Wolf", type = "Egg")
            }
        }
    })
