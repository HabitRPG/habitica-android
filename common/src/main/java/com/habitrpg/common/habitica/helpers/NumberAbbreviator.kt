package com.habitrpg.common.habitica.helpers

import android.content.Context
import kotlin.math.abs
import kotlin.math.min

object NumberAbbreviator {

    fun abbreviate(context: Context?, number: Float, numberOfDecimals: Int = 2, minForAbbrevation: Int = 0): String {
        return abbreviate(context, number.toDouble(), numberOfDecimals, minForAbbrevation)
    }

    fun abbreviate(context: Context?, number: Double, numberOfDecimals: Int = 2, minForAbbrevation: Int = 0): String {
        val decimalCount = if (number != 0.0 && number > -1 && number < 1 && numberOfDecimals == 0) 2 else numberOfDecimals
        val absNumber = abs(number)
        var usedNumber = absNumber
        var counter = 0
        while (usedNumber >= 1000 && absNumber >= minForAbbrevation) {
            counter++
            usedNumber /= 1000
        }
        val parts = usedNumber.toString().split(".")
        var result = parts[0]
        if (parts.size == 2) {
            var decimal = parts[1]
            decimal = decimal.substring(0, min(decimal.length, decimalCount))
            decimal = decimal.trimEnd('0')
            if (decimal.isNotEmpty()) {
                result = "$result.$decimal"
            }
        }
        if (number < 0) {
            result = "-$result"
        }
        return result + abbreviationForCounter(counter)
    }

    private fun abbreviationForCounter(counter: Int): String = when (counter) {
        0 -> ""
        1 -> "k"
        2 -> "m"
        3 -> "b"
        4 -> "t"
        5 -> "q"
        else -> ""
    }
}
