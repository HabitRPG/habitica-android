package com.habitrpg.android.habitica.models.shops

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class ShopItemTest :
    WordSpec({
        "imageName" should {
            "return icon_ if purchaseType is customization" {
                val item = ShopItem()
                item.purchaseType = "customization"
                item.imageName = "hair_1"
                item.imageName shouldBe "icon_hair_1"
            }

            "not add icon_ twice" {
                val item = ShopItem()
                item.purchaseType = "customization"
                item.imageName = "icon_hair_2"
                item.imageName shouldBe "icon_hair_2"
            }

            "return with shop_ prefix if no imageName is set" {
                val item = ShopItem()
                item.key = "item1"
                item.imageName shouldBe "shop_item1"
            }

            "return imageName if it is set" {
                val item = ShopItem()
                item.imageName = "test"
                item.imageName shouldBe "test"
            }

            "remove whitespaces from imageName" {
                val item = ShopItem()
                item.imageName = "test test"
                item.imageName shouldBe "test"
            }
        }
    })
