package com.habitrpg.android.habitica.helpers

import android.content.Context
import com.habitrpg.android.habitica.R
import java.math.RoundingMode
import java.text.DecimalFormat

object NumberAbbreviator {

    fun abbreviate(context: Context, number: Double, numberOfDecimals: Int = 2): String {
        var usedNumber = number
        var counter = 0
        while (usedNumber >= 1000) {
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

    private fun abbreviationForCounter(context: Context, counter: Int): String = when (counter) {
        1 -> context.getString(R.string.thousand_abbrev)
        2 -> context.getString(R.string.million_abbrev)
        3 -> context.getString(R.string.billion_abbrev)
        4 -> context.getString(R.string.trillion_abbrev)
        else -> ""
    }
}
