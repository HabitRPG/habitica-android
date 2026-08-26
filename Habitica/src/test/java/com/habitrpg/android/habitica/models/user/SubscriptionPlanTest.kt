package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.helpers.HabiticaProduct
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Date

class SubscriptionPlanTest :
    WordSpec({
        lateinit var plan: SubscriptionPlan
        beforeEach {
            plan = SubscriptionPlan()
            plan.customerId = "fake_customer_id"
            plan.planId = "test"
        }

        "isActive" should {
            "true if user has valid subscription" {
                plan.isActive shouldBe true
            }
            "true if user has cancelled subscription with remaining time" {
                plan.dateTerminated = Date(Date().time + 10000)
                plan.isActive shouldBe true
            }
            "false if user has cancelled subscription without remaining time" {
                plan.dateTerminated = Date(Date().time - 10000)
                plan.isActive shouldBe false
            }
        }

        "isGiftedSub" should {
            "true if paymentMethod is Gift" {
                plan.paymentMethod = "Gift"
                plan.isGiftedSub shouldBe true
            }
            "true if customerId is Gift" {
                plan.customerId = "Gift"
                plan.isGiftedSub shouldBe true
            }
            "false if user has not gifted sub" {
                plan.paymentMethod = "Credit Card"
                plan.isGiftedSub shouldBe false
            }
        }

        "isTerminated" should {
            "true is dateTerminated is not null" {
                plan.dateTerminated = Date()
                plan.isTerminated shouldBe true
            }
            "false if dateTerminated is null" {
                plan.isTerminated shouldBe false
            }
        }

        "totalNumberOfGems" should {
            "0 without an active subscription" {
                plan.customerId = null
                plan.totalNumberOfGems shouldBe 0
            }

            "24 without extra consecutive bonus" {
                plan.totalNumberOfGems shouldBe 24
            }

            "40 with extra consecutive bonus" {
                plan.consecutive = SubscriptionPlanConsecutive()
                plan.consecutive?.gemCapExtra = 16
                plan.totalNumberOfGems shouldBe 40
            }
        }

        "totalNumberOfGemsAlways" should {
            "24 without extra consecutive bonus" {
                plan.totalNumberOfGemsAlways shouldBe 24
            }

            "40 with extra consecutive bonus" {
                plan.consecutive = SubscriptionPlanConsecutive()
                plan.consecutive?.gemCapExtra = 16
                plan.totalNumberOfGemsAlways shouldBe 40
            }

            "30 with inactive subscription" {
                plan.customerId = null
                plan.consecutive = SubscriptionPlanConsecutive()
                plan.consecutive?.gemCapExtra = 6
                plan.totalNumberOfGemsAlways shouldBe 30
            }
        }

        "numberOfGemsLeft" should {
            "0 without an active subscription" {
                plan.customerId = null
                plan.numberOfGemsLeft shouldBe 0
            }

            "according to already purchased amount" {
                plan.gemsBought = 10
                plan.numberOfGemsLeft shouldBe 14
            }

            "according to already purchased amount with bonus" {
                plan.consecutive = SubscriptionPlanConsecutive()
                plan.consecutive?.gemCapExtra = 10
                plan.gemsBought = 10
                plan.numberOfGemsLeft shouldBe 24
            }
        }

        "monthsSubscribed" should {
            "0 without active subscription" {
                plan.customerId = null
                plan.monthsSubscribed shouldBe 0
            }

            "according to consecutive count" {
                plan.consecutive = SubscriptionPlanConsecutive()
                plan.consecutive?.count = 20
                plan.cumulativeCount = 10
                plan.monthsSubscribed shouldBe 20
            }

            "according to cumulative count" {
                plan.cumulativeCount = 20
                plan.consecutive?.count = 10
                plan.monthsSubscribed shouldBe 20
            }
        }

        "habiticaProduct" should {
            "1 month" {
                plan.planId = "basic_earned"
                plan.habiticaProduct shouldBe HabiticaProduct.SUBSCRIPTION_1_MONTH
            }

            "3 months" {
                plan.planId = "basic_3mo"
                plan.habiticaProduct shouldBe HabiticaProduct.SUBSCRIPTION_3_MONTH
            }

            "6 months" {
                plan.planId = "basic_6mo"
                plan.habiticaProduct shouldBe HabiticaProduct.SUBSCRIPTION_6_MONTH
            }

            "12 months" {
                plan.planId = "basic_12mo"
                plan.habiticaProduct shouldBe HabiticaProduct.SUBSCRIPTION_12_MONTH
            }
        }
    })
