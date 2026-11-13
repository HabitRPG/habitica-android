package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.social.ChatMessage
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ChatMessageDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "read all message fields and only count positive likes" {
                val json = """{
                    "id": "m1", "text": "Hello", "timestamp": 1443445200000,
                    "likes": {"u1": true, "u2": false, "u3": true},
                    "flagCount": 2, "uuid": "u9", "user": "Name", "username": "name",
                    "sent": true, "contributor": {"level": 4}, "backer": {"tier": 3},
                    "userStyles": {"stats": {"lvl": 5}}
                }"""
                val message = gson.fromJson(json, ChatMessage::class.java)
                message.id shouldBe "m1"
                message.text shouldBe "Hello"
                message.timestamp shouldBe 1443445200000
                message.likes?.map { it.id } shouldBe listOf("u1", "u3")
                message.likeCount shouldBe 2
                message.flagCount shouldBe 2
                message.uuid shouldBe "u9"
                message.user shouldBe "Name"
                message.username shouldBe "name"
                message.sent shouldBe true
                message.contributor?.level shouldBe 4
                message.backer?.tier shouldBe 3
                message.userStyles?.stats?.lvl shouldBe 5
            }

            "wrap a plain contributor string" {
                val message = gson.fromJson("""{"contributor": "Artisan"}""", ChatMessage::class.java)
                message.contributor?.text shouldBe "Artisan"
            }

            "ignore null or non-primitive text, username and contributor" {
                val json = """{"text": null, "username": {}, "contributor": null}"""
                val message = gson.fromJson(json, ChatMessage::class.java)
                message.text.shouldBeNull()
                message.username.shouldBeNull()
                message.contributor.shouldBeNull()
            }
        }
    })
