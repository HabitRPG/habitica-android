package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.helpers.NumberAbbreviator.abbreviate
import com.habitrpg.android.habitica.R
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NumberAbbreviatorTest {
    @MockK private lateinit var context: Context
    @BeforeEach
    fun setUp() {
        every { context.getString(R.string.thousand_abbrev) } returns "k"
        every { context.getString(R.string.million_abbrev) } returns "m"
        every { context.getString(R.string.billion_abbrev) } returns "b"
        every { context.getString(R.string.trillion_abbrev) } returns "t"
    }

    @Test
    fun testThatItDoesntAbbreviatesSmallNumbers() {
        abbreviate(context, 215.0, 2) shouldBe "215"
        abbreviate(context, 2.05, 2) shouldBe "2.05"
    }

    @Test
    fun testThatItAbbreviatesThousand() {
        abbreviate(context, 1550.0, 2) shouldBe "1.55k"
    }

    @Test
    fun testThatItAbbreviatesMillion() {
        abbreviate(context, 9990000.0, 2) shouldBe "9.99m"
    }

    @Test
    fun testThatItAbbreviatesBillion() {
        abbreviate(context, 1990000000.0, 2) shouldBe "1.99b"
    }

    @Test
    fun testThatItAbbreviatesThousandWithoutAdditionalDecimals() {
        abbreviate(context, 1000.0, 2) shouldBe "1k"
        abbreviate(context, 1500.0, 2) shouldBe "1.5k"
    }

    @Test
    fun voidtestThatitRoundsCorrectly() {
        abbreviate(context, 9999.0, 2) shouldBe "9.99k"
    }
}