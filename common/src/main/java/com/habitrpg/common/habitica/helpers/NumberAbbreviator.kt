package com.habitrpg.common.habitica.helpers

import android.content.Context
import com.habitrpg.common.habitica.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

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
        var pattern = "###"
        if (decimalCount > 0) {
            pattern = ("$pattern.").padEnd(4 + decimalCount, '#')
        }
        if (number < 0) {
            pattern = "-$pattern"
        }

        // Initially use US symbols for formatting
        val symbols = DecimalFormatSymbols.getInstance(Locale.US)
        val formatter = DecimalFormat(
            pattern + abbreviationForCounter(context, counter)
                .replace(".", "")
                .replace(",", ""),
            symbols
        )
        formatter.roundingMode = RoundingMode.FLOOR
        var formattedString = formatter.format(usedNumber)

        // Get the decimal separator of the current locale and replace the decimal separator with the current locale.
        val currentLocaleSymbols = DecimalFormatSymbols.getInstance()
        val currentLocaleDecimalSeparator = currentLocaleSymbols.decimalSeparator
        formattedString = formattedString.replace('.', currentLocaleDecimalSeparator)

        return formattedString
    }


    private fun abbreviationForCounter(context: Context?, counter: Int): String = when (counter) {
        1 -> context?.getString(R.string.thousand_abbrev) ?: "k"
        2 -> context?.getString(R.string.million_abbrev) ?: "m"
        3 -> context?.getString(R.string.billion_abbrev) ?: "b"
        4 -> context?.getString(R.string.trillion_abbrev) ?: "t"
        5 -> context?.getString(R.string.quadrillion_abbrev) ?: "q"
        else -> ""
    }
}
