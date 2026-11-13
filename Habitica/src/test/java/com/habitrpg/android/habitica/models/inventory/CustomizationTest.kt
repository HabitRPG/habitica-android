package com.habitrpg.android.habitica.models.inventory

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Date

class CustomizationTest :
    WordSpec({
        "purchaseable" should {
            "return true if date is between availableFrom and availableUntil" {
                val customization = Customization()
                customization.availableFrom = Date(System.currentTimeMillis() - 1000)
                customization.availableUntil = Date(System.currentTimeMillis() + 1000)
                customization.purchasable shouldBe true
            }

            "return false if date is after availableUntil" {
                val customization = Customization()
                customization.availableFrom = Date(System.currentTimeMillis() - 1000)
                customization.availableUntil = Date(System.currentTimeMillis() - 1000)
                customization.purchasable shouldBe false
            }

            "return false if date is before availableFrom" {
                val customization = Customization()
                customization.availableFrom = Date(System.currentTimeMillis() + 1000)
                customization.availableUntil = Date(System.currentTimeMillis() + 1000)
                customization.purchasable shouldBe false
            }

            "return true if availableFrom is null" {
                val customization = Customization()
                customization.availableFrom = null
                customization.availableUntil = Date(System.currentTimeMillis() + 1000)
                customization.purchasable shouldBe true
            }
            "return true if availableUntil is null" {
                val customization = Customization()
                customization.availableFrom = Date(System.currentTimeMillis() - 1000)
                customization.availableUntil = null
                customization.purchasable shouldBe true
            }
        }
    })
