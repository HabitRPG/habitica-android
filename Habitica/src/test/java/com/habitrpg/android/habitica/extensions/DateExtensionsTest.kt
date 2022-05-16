package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import io.kotest.matchers.shouldBe
import java.util.Date

class DateExtensionsTest : BaseAnnotationTestCase() {

    @Test
    fun testGetShortRemainingStringWithDay() {
        (Date().time + 2077400000L).getShortRemainingString() shouldBe "24d 1h 3m"
    }

    @Test
    fun testGetShortRemainingStringWithHour() {
        (Date().time + 20774000L).getShortRemainingString() shouldBe "5h 46m"
    }

    @Test
    fun testGetShortRemainingStringWithMinute() {
        (Date().time + 2077400L).getShortRemainingString() shouldBe "34m 37s"
    }
}
