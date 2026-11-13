package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.api.GSonFactoryCreator
import com.habitrpg.android.habitica.models.user.Purchases
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PurchasedDeserializerTest :
    WordSpec({
        val gson = GSonFactoryCreator.createGson()

        "deserialize" should {
            "flatten customizations including hair categories" {
                val json = """{
                    "background": {"beach": true},
                    "skin": {"pale": false},
                    "hair": {"color": {"red": true}},
                    "plan": {"customerId": "customer-1"}
                }"""
                val purchases = gson.fromJson(json, Purchases::class.java)
                purchases.customizations?.map { listOf(it.type, it.category, it.key, it.purchased) } shouldBe
                    listOf(
                        listOf("background", null, "beach", true),
                        listOf("skin", null, "pale", false),
                        listOf("hair", "color", "red", true),
                    )
                purchases.plan?.customerId shouldBe "customer-1"
            }

            "handle a missing plan" {
                gson.fromJson("{}", Purchases::class.java).plan.shouldBeNull()
            }
        }
    })
