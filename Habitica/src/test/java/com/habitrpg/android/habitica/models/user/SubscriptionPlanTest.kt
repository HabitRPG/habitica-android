package com.habitrpg.android.habitica.models.user

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Date

class SubscriptionPlanTest : WordSpec({
    lateinit var plan: SubscriptionPlan
    beforeEach {
        plan = SubscriptionPlan()
    }
    "isActive" should {
        "true if user has valid subscription" {
            plan.customerId = "some-id"
            plan.isActive shouldBe true
        }
        "true if user has cancelled subscription with remaining time" {
            plan.customerId = "some-id"
            plan.dateTerminated = Date(Date().time + 10000)
            plan.isActive shouldBe true
        }
        "false if user has cancelled subscription without remaining time" {
            plan.customerId = "some-id"
            plan.dateTerminated = Date(Date().time - 10000)
            plan.isActive shouldBe false
        }
    }

    "totalNumberOfGems" should {
        beforeEach {
            plan.customerId = "some-id"
        }
        "0 without an active subscription" {
            plan.customerId = null
            plan.totalNumberOfGems shouldBe 0
        }

        "25 without extra consecutive bonus" {
            plan.totalNumberOfGems shouldBe 25
        }

        "35 with extra consecutive bonus" {
            plan.consecutive = SubscriptionPlanConsecutive()
            plan.consecutive?.gemCapExtra = 15
            plan.totalNumberOfGems shouldBe 40
        }
    }

    "numberOfGemsLeft" should {
        beforeEach {
            plan.customerId = "some-id"
        }
        "0 without an active subscription" {
            plan.customerId = null
            plan.numberOfGemsLeft shouldBe 0
        }

        "according to already purchased amount" {
            plan.gemsBought = 10
            plan.numberOfGemsLeft shouldBe 15
        }

        "according to already purchased amount with bonus" {
            plan.consecutive = SubscriptionPlanConsecutive()
            plan.consecutive?.gemCapExtra = 10
            plan.gemsBought = 10
            plan.numberOfGemsLeft shouldBe 25
        }
    }
})
