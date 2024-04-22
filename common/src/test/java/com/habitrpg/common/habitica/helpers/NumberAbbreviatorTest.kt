package com.habitrpg.common.habitica.helpers

import android.content.Context
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.Locale

class NumberAbbreviatorTest : StringSpec({
    val mockContext = mockk<Context>()
    beforeSpec {
        Locale.setDefault(Locale.US)
        every { mockContext.getString(com.habitrpg.common.habitica.R.string.thousand_abbrev) } returns "k"
        every { mockContext.getString(com.habitrpg.common.habitica.R.string.million_abbrev) } returns "m"
        every { mockContext.getString(com.habitrpg.common.habitica.R.string.billion_abbrev) } returns "b"
        every { mockContext.getString(com.habitrpg.common.habitica.R.string.trillion_abbrev) } returns "t"
        every { mockContext.getString(com.habitrpg.common.habitica.R.string.quadrillion_abbrev) } returns "q"
    }

    withData(
        Triple(215.0, "215", 2),
        Triple(2.05, "2.05", 2),
        Triple(5.406, "5.4", 2),
        Triple(-20.42, "-20.42", 2),
        Triple(2550.0, "2.55k", 2),
        Triple(-1020.42, "-1.02k", 2),
        Triple(9990000.0, "9.99m", 2),
        Triple(1990000000.0, "1.99b", 2),
        Triple(1990000000000.0, "1.99t", 2),
        Triple(-1990000000.42, "-1.99b", 2),
        Triple(1000.0, "1k", 2),
        Triple(1500.0, "1.5k", 2),
        Triple(1500.0, "1k", 0),
        Triple(-1302.42, "-1.3k", 2),
        Triple(9999.0, "9.99k", 2),
        Triple(-20.42, "-20", 0),
        Triple(40.2412, "40", 0),
        Triple(0.5, "0.5", 0),
        Triple(0.328, "0.32", 0),
        Triple(-0.99, "-0.99", 0),
    ) { (input, output, decimals) ->
        NumberAbbreviator.abbreviate(mockContext, input, decimals) shouldBe output
    }

    afterSpec { clearMocks(mockContext) }
})
