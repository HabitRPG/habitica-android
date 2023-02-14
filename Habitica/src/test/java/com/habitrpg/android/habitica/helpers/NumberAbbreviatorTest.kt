package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.NumberAbbreviator.abbreviate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.Locale

class NumberAbbreviatorTest : StringSpec({
    val mockContext = mockk<Context>()
    beforeEach {
        Locale.setDefault(Locale.US)
        every { mockContext.getString(R.string.thousand_abbrev) } returns "k"
        every { mockContext.getString(R.string.million_abbrev) } returns "m"
        every { mockContext.getString(R.string.billion_abbrev) } returns "b"
        every { mockContext.getString(R.string.trillion_abbrev) } returns "t"
    }

    "should not abbreviate small numbers" {
        abbreviate(mockContext, 215.0, 2) shouldBe "215"
        abbreviate(mockContext, 2.05, 2) shouldBe "2.05"
    }

    "should abbreviate thousands" {
        abbreviate(mockContext, 1550.0, 2) shouldBe "1.55k"
    }

    "should abbreviate millions" {
        abbreviate(mockContext, 9990000.0, 2) shouldBe "9.99m"
    }

    "should abbreviate billions" {
        abbreviate(mockContext, 1990000000.0, 2) shouldBe "1.99b"
    }

    "should abbreviate trillions" {
        abbreviate(mockContext, 1990000000000.0, 2) shouldBe "1.99t"
    }

    "should abbreviate thousands without additional decimals" {
        abbreviate(mockContext, 1000.0, 2) shouldBe "1k"
        abbreviate(mockContext, 1500.0, 2) shouldBe "1.5k"
        abbreviate(mockContext, 1500.0, 0) shouldBe "1k"
    }

    "should round correctly" {
        abbreviate(mockContext, 9999.0, 2) shouldBe "9.99k"
    }

    "should force decimals for numbers between -1 and 1" {
        abbreviate(mockContext, 0.5, 0) shouldBe "0.5"
        abbreviate(mockContext, 0.3248, 0) shouldBe "0.32"
        abbreviate(mockContext, -0.99, 0) shouldBe "-0.99"
    }

    afterEach { clearMocks(mockContext) }
})
