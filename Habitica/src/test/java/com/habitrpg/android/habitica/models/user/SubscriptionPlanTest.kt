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

    "monthsUntilNextHourglass" should {
        beforeEach {
            plan.consecutive = SubscriptionPlanConsecutive()
            plan.consecutive?.count = 0
            plan.dateTerminated = null
        }

        "months until next hourglass with initial basic sub" {
            plan.planId = SubscriptionPlan.PLANID_BASIC
            plan.monthsUntilNextHourglass shouldBe 3
        }

        "months until receiving first hourglass with basic sub" {
            plan.consecutive?.count = 2
            plan.planId = SubscriptionPlan.PLANID_BASIC
            plan.monthsUntilNextHourglass shouldBe 1
        }

        "months until next hourglass with basic sub after receiving initial hourglass" {
            plan.consecutive?.count = 3
            plan.planId = SubscriptionPlan.PLANID_BASIC
            plan.monthsUntilNextHourglass shouldBe 3
        }

        "months until next hourglass with three month sub" {
            plan.consecutive?.offset = 3
            plan.planId = SubscriptionPlan.PLANID_BASIC3MONTH
            plan.monthsUntilNextHourglass shouldBe 3
        }

        "months until next hourglass with six month sub" {
            plan.consecutive?.offset = 6
            plan.planId = SubscriptionPlan.PLANID_BASIC6MONTH
            plan.monthsUntilNextHourglass shouldBe 6
        }

        "months until next hourglass with 12 month sub" {
            plan.consecutive?.offset = 12
            plan.planId = SubscriptionPlan.PLANID_BASIC12MONTH
            plan.monthsUntilNextHourglass shouldBe 12
        }
    }
})
