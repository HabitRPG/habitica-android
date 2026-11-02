package com.habitrpg.android.habitica.utils

import com.habitrpg.android.habitica.models.user.Purchases
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PurchasedDeserializerTest :
    SerializerSpec({

        "deserialize" should {
            "flatten customizations including hair categories" {
                val json = """{
                    "background": {"beach": true},
                    "skin": {"pale": false},
                    "hair": {"color": {"red": true}},
                    "plan": {"customerId": "customer-1"}
                }"""
                val purchases = parse<Purchases>(json)
                purchases.customizations?.map { listOf(it.type, it.category, it.key, it.purchased) } shouldBe
                    listOf(
                        listOf("background", null, "beach", true),
                        listOf("skin", null, "pale", false),
                        listOf("hair", "color", "red", true),
                    )
                purchases.plan?.customerId shouldBe "customer-1"
            }

            "handle a missing plan" {
                parse<Purchases>("{}").plan.shouldBeNull()
            }
        }
    })
