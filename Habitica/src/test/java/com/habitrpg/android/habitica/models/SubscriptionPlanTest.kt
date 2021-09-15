package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import org.junit.Before
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.Exception
import java.util.Calendar
import java.util.Date
import kotlin.Throws

class SubscriptionPlanTest {
    private var plan: SubscriptionPlan? = null
    @BeforeEach
    fun setUp() {
        plan = SubscriptionPlan()
        plan!!.customerId = "fake_customer_id"
        plan!!.planId = "test"
    }

    @get:Test
    val isActiveForNoTerminationDate: Unit
        get() {
            Assertions.assertTrue(plan!!.isActive)
        }

    @get:Test
    val isActiveForLaterTerminationDate: Unit
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, 1)
            plan!!.dateTerminated = calendar.time
            Assertions.assertTrue(plan!!.isActive)
        }

    @get:Test
    val isInactiveForEarlierTerminationDate: Unit
        get() {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DATE, -1)
            plan!!.dateTerminated = calendar.time
            Assertions.assertFalse(plan!!.isActive)
        }
}