package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.Calendar
import java.util.Date

class SubscriptionPlanTest : BaseAnnotationTestCase() {
    private var plan: SubscriptionPlan? = null
    @AnnotationSpec.BeforeEach
    fun setUp() {
        plan = SubscriptionPlan()
        plan!!.customerId = "fake_customer_id"
        plan!!.planId = "test"
    }

    @get:Test
    val isActiveForNoTerminationDate: Unit
        get() {
            plan?.isActive shouldBe true
        }

    @get:Test
    val isActiveForLaterTerminationDate: Unit
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, 1)
            plan!!.dateTerminated = calendar.time
            plan?.isActive shouldBe true
        }

    @get:Test
    val isInactiveForEarlierTerminationDate: Unit
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, -1)
            plan!!.dateTerminated = calendar.time
            plan?.isActive shouldBe false
        }
}
