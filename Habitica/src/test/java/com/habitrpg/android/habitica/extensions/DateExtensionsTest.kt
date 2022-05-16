package com.habitrpg.android.habitica.extensions

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import io.kotest.matchers.shouldBe
import java.util.Date

class DateExtensionsTest : BaseAnnotationTestCase() {

    @Test
    fun testGetShortRemainingStringWithDay() {
        (Date().time + 2077400000L).getShortRemainingString() shouldBe "24d 1h 3m"
        (Date().time + 2091600000L).getShortRemainingString() shouldBe "24d 5h"
        (Date().time + 2074200000L).getShortRemainingString() shouldBe "24d 10m"
        (Date().time + 2073600000L).getShortRemainingString() shouldBe "24d"
    }

    @Test
    fun testGetShortRemainingStringWithHour() {
        (Date().time + 20774000L).getShortRemainingString() shouldBe "5h 46m"
        (Date().time + 82800000L).getShortRemainingString() shouldBe "23h"
    }

    @Test
    fun testGetShortRemainingStringWithMinute() {
        (Date().time + 2077400L).getShortRemainingString() shouldBe "34m 37s"
        (Date().time + 2400000L).getShortRemainingString() shouldBe "40m"
    }
}
