package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import io.kotest.matchers.shouldBe
import java.util.Date

class DateExtensionsTest : BaseAnnotationTestCase() {

    @Test
    fun testGetShortRemainingStringWithDay() {
        "24d 1h 3m" shouldBe (Date().time + 2077400000L).getShortRemainingString()
    }

    @Test
    fun testGetShortRemainingStringWithHour() {
        "5h 46m" shouldBe (Date().time + 20774000L).getShortRemainingString()
    }

    @Test
    fun testGetShortRemainingStringWithMinute() {
        "34m 37s" shouldBe (Date().time + 2077400L).getShortRemainingString()
    }
}
