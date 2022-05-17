package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.NumberAbbreviator.abbreviate
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.*

class NumberAbbreviatorTest : StringSpec({
    val mockContext = mockk<Context>()
    beforeEach {
        Locale.setDefault(Locale.US)
        every { mockContext.getString(R.string.thousand_abbrev) } returns "k"
        every { mockContext.getString(R.string.million_abbrev) } returns "m"
        every { mockContext.getString(R.string.billion_abbrev) } returns "b"
        every { mockContext.getString(R.string.trillion_abbrev) } returns "t"
    }

    "doesn't abbreviate small numbers" {
        abbreviate(mockContext, 215.0, 2) shouldBe "215"
        abbreviate(mockContext, 2.05, 2) shouldBe "2.05"
    }

    "it abbreviates thousands" {
        abbreviate(mockContext, 1550.0, 2) shouldBe "1.55k"
    }

    "it abbreviates millions" {
        abbreviate(mockContext, 9990000.0, 2) shouldBe "9.99m"
    }

    "it abbreviates billions" {
        abbreviate(mockContext, 1990000000.0, 2) shouldBe "1.99b"
    }

    "it abbreviates trillions" {
        abbreviate(mockContext, 1990000000000.0, 2) shouldBe "1.99t"
    }

    "it abbreviates thousands without additional decimals" {
        abbreviate(mockContext, 1000.0, 2) shouldBe "1k"
        abbreviate(mockContext, 1500.0, 2) shouldBe "1.5k"
        abbreviate(mockContext, 1500.0, 0) shouldBe "1k"
    }

    "it rounds correctly" {
        abbreviate(mockContext, 9999.0, 2) shouldBe "9.99k"
    }

    afterEach { clearMocks(mockContext) }
})
