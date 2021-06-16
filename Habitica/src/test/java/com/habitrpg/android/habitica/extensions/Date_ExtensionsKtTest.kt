package com.habitrpg.android.habitica.extensions

import junit.framework.TestCase
import java.util.*

class DateExtensionsTest : TestCase() {

    fun testGetShortRemainingStringWithDay() {
        assertEquals("24d 1h 3m", (Date().time + 2077400000L).getShortRemainingString())
    }

    fun testGetShortRemainingStringWithHour() {
        assertEquals("5h 46m", (Date().time + 20774000L).getShortRemainingString())
    }

    fun testGetShortRemainingStringWithMinute() {
        assertEquals("34m 37s", (Date().time + 2077400L).getShortRemainingString())
    }
}