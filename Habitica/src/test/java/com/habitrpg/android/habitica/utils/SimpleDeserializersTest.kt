package com.habitrpg.android.habitica.utils

import com.google.gson.JsonParseException
import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.FAQArticle
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.inventory.QuestCollect
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import com.habitrpg.android.habitica.models.social.FindUsernameResult
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import com.google.gson.reflect.TypeToken
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.realm.RealmList

class SimpleDeserializersTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "FeedResponseDeserializer" should {
            "read the raw integer value" {
                gson.fromJson("42", FeedResponse::class.java).value shouldBe 42
            }
        }

        "InviteResponseDeserializer" should {
            "parse a string as an email invite" {
                gson.fromJson("\"a@b.c\"", InviteResponse::class.java) shouldBe InviteResponse.EmailInvite("a@b.c")
            }

            "parse an object as a user invite" {
                val json = """{"id": "u1", "name": "Name", "inviter": "u2"}"""
                gson.fromJson(json, InviteResponse::class.java) shouldBe InviteResponse.UserInvite("u1", "Name", "u2")
            }

            "reject other shapes" {
                shouldThrow<JsonParseException> { gson.fromJson("12", InviteResponse::class.java) }
            }
        }

        "SocialAuthenticationDeserializer" should {
            "collect emails given as strings or value objects" {
                val json = """{"emails": ["a@b.c", {"value": "d@e.f"}, 5]}"""
                gson.fromJson(json, SocialAuthentication::class.java).emails shouldBe listOf("a@b.c", "d@e.f", "5")
            }

            "ignore a non-array emails field" {
                gson.fromJson("""{"emails": "a@b.c"}""", SocialAuthentication::class.java).emails shouldBe emptyList()
            }
        }

        "FindUsernameResultDeserializer" should {
            "read contributor and auth" {
                val json = """{"contributor": {"level": 3}, "auth": {"local": {"username": "tester"}}}"""
                val result = gson.fromJson(json, FindUsernameResult::class.java)
                result.contributor?.level shouldBe 3
                result.formattedUsername shouldBe "@tester"
            }

            "leave fields empty when absent" {
                val result = gson.fromJson("{}", FindUsernameResult::class.java)
                result.contributor.shouldBeNull()
                result.authentication.shouldBeNull()
            }
        }

        "QuestCollectDeserializer" should {
            "map collect entries by key" {
                val json = """{"soapBars": {"count": 20, "text": "Bars of Soap"}}"""
                val type = object : TypeToken<RealmList<QuestCollect>>() {}.type
                val items: RealmList<QuestCollect> = gson.fromJson(json, type)
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
                val type = object : TypeToken<RealmList<QuestDropItem>>() {}.type
                val items: RealmList<QuestDropItem> = gson.fromJson(json, type)
                items.map { it.key to it.count } shouldBe listOf("Egg" to 2, "Gold" to 1)
            }
        }

        "TutorialStepListDeserializer" should {
            "read common and android groups in order" {
                val json = """{"common": {"habits": true}, "android": {"party": false}, "ios": {"x": true}}"""
                val type = object : TypeToken<RealmList<TutorialStep>>() {}.type
                val steps: RealmList<TutorialStep> = gson.fromJson(json, type)
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
                val type = object : TypeToken<RealmList<FAQArticle>>() {}.type
                val articles: RealmList<FAQArticle> = gson.fromJson(json, type)
                articles.map { Triple(it.position, it.question, it.answer) } shouldBe
                    listOf(Triple(0, "Q1", "A1"), Triple(1, "Q2", "W2"))
            }
        }
    })
