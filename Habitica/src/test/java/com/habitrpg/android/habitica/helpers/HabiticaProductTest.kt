package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.R
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class HabiticaProductTest :
    WordSpec({
        "forSku" should {
            "find every product by its sku" {
                HabiticaProduct.entries.forEach {
                    HabiticaProduct.forSku(it.sku) shouldBe it
                }
            }

            "return null for unknown skus" {
                HabiticaProduct.forSku("com.habitrpg.android.habitica.iap.1000gems").shouldBeNull()
                HabiticaProduct.forSku("").shouldBeNull()
            }
        }

        "skus" should {
            "be unique" {
                HabiticaProduct.entries.map { it.sku }.toSet().size shouldBe HabiticaProduct.entries.size
            }
        }

        "getSubscriptionDuration" should {
            "return months for renewing and non renewing subscriptions" {
                HabiticaProduct.allSubscriptionTypes.map { it.getSubscriptionDuration() } shouldContainExactly listOf(1, 3, 6, 12)
                HabiticaProduct.allSubscriptionNoRenewTypes.map { it.getSubscriptionDuration() } shouldContainExactly listOf(1, 3, 6, 12)
            }

            "return 0 for non subscription products" {
                HabiticaProduct.allGemTypes.forEach { it.getSubscriptionDuration() shouldBe 0 }
                HabiticaProduct.JUBILANT_GRYPHATRICE.getSubscriptionDuration() shouldBe 0
            }
        }

        "getSubCode" should {
            "map renewing subscriptions to server plan keys" {
                HabiticaProduct.allSubscriptionTypes.map { it.getSubCode() } shouldContainExactly
                    listOf("basic_earned", "basic_3mo", "basic_6mo", "basic_12mo")
            }

            "be empty for everything else" {
                HabiticaProduct.allSubscriptionNoRenewTypes.forEach { it.getSubCode() shouldBe "" }
                HabiticaProduct.allGemTypes.forEach { it.getSubCode() shouldBe "" }
            }
        }

        "getGemAmount" should {
            "return the regular gem amounts" {
                HabiticaProduct.allGemTypes.map { it.getGemAmount(false) } shouldContainExactly listOf(4, 21, 42, 84)
            }

            "return the increased amounts during a gem sale" {
                HabiticaProduct.allGemTypes.map { it.getGemAmount(true) } shouldContainExactly listOf(5, 30, 60, 125)
            }

            "return 0 for non gem products" {
                HabiticaProduct.SUBSCRIPTION_1_MONTH.getGemAmount(false) shouldBe 0
                HabiticaProduct.SUBSCRIPTION_1_MONTH.getGemAmount(true) shouldBe 0
                HabiticaProduct.JUBILANT_GRYPHATRICE.getGemAmount(false) shouldBe 0
            }
        }

        "product groups" should {
            "not overlap" {
                HabiticaProduct.allSubscriptionTypes shouldNotContainAnyOf HabiticaProduct.allSubscriptionNoRenewTypes
                HabiticaProduct.allSubscriptionTypes shouldNotContainAnyOf HabiticaProduct.allGemTypes
            }
        }

        "recurranceStringRes" should {
            "map renewing subscriptions to their duration label" {
                HabiticaProduct.SUBSCRIPTION_1_MONTH.recurranceStringRes shouldBe R.string.one_month
                HabiticaProduct.SUBSCRIPTION_3_MONTH.recurranceStringRes shouldBe R.string.three_months
                HabiticaProduct.SUBSCRIPTION_6_MONTH.recurranceStringRes shouldBe R.string.six_months
                HabiticaProduct.SUBSCRIPTION_12_MONTH.recurranceStringRes shouldBe R.string.twelve_months
            }

            "throw for non subscription products" {
                shouldThrow<IllegalArgumentException> { HabiticaProduct.PURCHASE_21_GEMS.recurranceStringRes }
            }
        }
    })
