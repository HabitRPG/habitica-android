package com.habitrpg.android.habitica.models.user

import android.content.Context
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import java.util.Date

class SubscriptionPlanTest : WordSpec({
    lateinit var plan: SubscriptionPlan
    lateinit var mockContext: Context
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
})
