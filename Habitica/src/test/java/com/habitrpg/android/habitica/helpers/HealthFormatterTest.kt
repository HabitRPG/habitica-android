package com.habitrpg.android.habitica.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class HealthFormatterTest {
    @Test
    fun shouldRoundValuesGreaterThanOneDown() {
        assertEquals(49.0, HealthFormatter.format(49.9), DELTA)
        assertEquals(9.0, HealthFormatter.format(9.9999), DELTA)
        assertEquals(1.0, HealthFormatter.format(1.9), DELTA)
        assertEquals(1.0, HealthFormatter.format(1.0001), DELTA)

        assertEquals("49", HealthFormatter.formatToString(49.9, Locale.US))
        assertEquals("9", HealthFormatter.formatToString(9.9999, Locale.US))
        assertEquals("1", HealthFormatter.formatToString(1.9, Locale.US))
        assertEquals("1", HealthFormatter.formatToString(1.0001, Locale.US))
    }

    @Test
    fun shouldRoundValuesBetweenZeroAndOneUpToOneDecimalPlace() {
        assertEquals(1.0, HealthFormatter.format(0.99), DELTA)
        assertEquals(0.2, HealthFormatter.format(0.11), DELTA)
        assertEquals(0.1, HealthFormatter.format(0.0001), DELTA)

        assertEquals("1", HealthFormatter.formatToString(0.99, Locale.US))
        assertEquals("0.2", HealthFormatter.formatToString(0.11, Locale.US))
        assertEquals("0.1", HealthFormatter.formatToString(0.0001, Locale.US))
    }

    @Test
    fun shouldRoundNegativeValuesDown() {
        assertEquals(-1.0, HealthFormatter.format(-0.1), DELTA)
        assertEquals(-2.0, HealthFormatter.format(-2.0), DELTA)

        assertEquals("-1", HealthFormatter.formatToString(-0.1, Locale.US))
        assertEquals("-2", HealthFormatter.formatToString(-2.0, Locale.US))
    }

    @Test
    fun shouldLeaveAcceptableValuesAsTheyAre() {
        assertEquals(20.0, HealthFormatter.format(20), DELTA)
        assertEquals(0.0, HealthFormatter.format(0), DELTA)
        assertEquals(0.9, HealthFormatter.format(0.9), DELTA)

        assertEquals("20", HealthFormatter.formatToString(20, Locale.US))
        assertEquals("0", HealthFormatter.formatToString(0, Locale.US))
        assertEquals("0.9", HealthFormatter.formatToString(0.9, Locale.US))
    }

    companion object {
        private const val DELTA = 0.0
    }
}