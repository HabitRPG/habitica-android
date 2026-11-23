package com.habitrpg.shared.habitica.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DoubleExtensionsTest {
    @Test
    fun roundsToGivenDecimals() {
        assertEquals(1.23, 1.234.round(2))
        assertEquals(1.24, 1.235.round(2), 1e-9)
        assertEquals(2.0, 1.5.round(0))
        assertEquals(0.0, 0.04.round(1))
    }

    @Test
    fun roundsNegativeHalvesTowardsPositiveInfinity() {
        // roundToInt rounds half up, so -1.5 becomes -1, not -2
        assertEquals(-1.0, (-1.5).round(0))
        assertEquals(-2.0, (-1.6).round(0))
    }

    @Test
    fun negativeDecimalsRoundToTens() {
        assertEquals(120.0, 123.0.round(-1))
    }

    @Test
    fun largeValuesAreClampedToIntRange() {
        // shortcut in round(): roundToInt clamps, so values above Int.MAX_VALUE / 10^decimals are wrong
        assertEquals(Int.MAX_VALUE / 100.0, 1e10.round(2))
    }

    @Test
    fun nanThrows() {
        assertFailsWith<IllegalArgumentException> { Double.NaN.round(1) }
    }
}
