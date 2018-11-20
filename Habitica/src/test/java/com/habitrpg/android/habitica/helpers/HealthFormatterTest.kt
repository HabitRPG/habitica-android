package com.habitrpg.android.habitica.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthFormatterTest {
    @Test
    fun shouldRoundValuesGreaterThanOneDown() {
        assertEquals(49.0, HealthFormatter.format(49.9), DELTA)
        assertEquals(9.0, HealthFormatter.format(9.9999), DELTA)
        assertEquals(1.0, HealthFormatter.format(1.9), DELTA)
        assertEquals(1.0, HealthFormatter.format(1.0001), DELTA)
    }

    @Test
    fun shouldRoundValuesBetweenZeroAndOneUpToOneDecimalPlace() {
        assertEquals(1.0, HealthFormatter.format(0.99), DELTA)
        assertEquals(0.2, HealthFormatter.format(0.11), DELTA)
        assertEquals(0.1, HealthFormatter.format(0.0001), DELTA)
    }

    @Test
    fun shouldRoundNegativeValuesDown() {
        assertEquals(-1.0, HealthFormatter.format(-0.1), DELTA)
        assertEquals(-2.0, HealthFormatter.format(-2.0), DELTA)
    }

    @Test
    fun shouldLeaveAcceptableValuesAsTheyAre() {
        assertEquals(20.0, HealthFormatter.format(20), DELTA)
        assertEquals(0.0, HealthFormatter.format(0), DELTA)
        assertEquals(0.9, HealthFormatter.format(0.9), DELTA)
    }

    companion object {
        private const val DELTA = 0.0
    }
}