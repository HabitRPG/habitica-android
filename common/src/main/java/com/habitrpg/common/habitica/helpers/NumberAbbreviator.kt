package com.habitrpg.common.habitica.helpers

import android.content.Context
import com.habitrpg.common.habitica.R
import java.math.RoundingMode
import java.text.DecimalFormat

object NumberAbbreviator {

    fun abbreviate(context: Context?, number: Double, numberOfDecimals: Int = 2, minForAbbrevation: Int = 0): String {
        var usedNumber = number
        var counter = 0
        while (usedNumber >= 1000 && number >= minForAbbrevation) {
            counter++
            usedNumber /= 1000
        }
        var pattern = "###"
        if (numberOfDecimals > 0) {
            pattern = ("$pattern.").padEnd(4 + numberOfDecimals, '#')
        }
        val formatter = DecimalFormat(pattern + abbreviationForCounter(context, counter).replace(".", ""))
        formatter.roundingMode = RoundingMode.FLOOR
        return formatter.format(usedNumber)
    }

    private fun abbreviationForCounter(context: Context?, counter: Int): String = when (counter) {
        1 -> context?.getString(R.string.thousand_abbrev) ?: "k"
        2 -> context?.getString(R.string.million_abbrev) ?: "m"
        3 -> context?.getString(R.string.billion_abbrev) ?: "b"
        4 -> context?.getString(R.string.trillion_abbrev) ?: "t"
        else -> ""
    }
}
