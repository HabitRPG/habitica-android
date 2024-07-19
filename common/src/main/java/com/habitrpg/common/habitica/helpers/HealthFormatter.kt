package com.habitrpg.common.habitica.helpers

import android.os.Build
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

object HealthFormatter {
    fun format(input: Int) = format(input.toDouble())

    @JvmStatic
    fun format(input: Double) =
        if (input < 1 && input > 0) {
            ceil(input * 10) / 10
        } else {
            floor(input)
        }

    fun formatToString(
        input: Int,
        locale: Locale = getDefaultLocale(),
    ) = formatToString(input.toDouble(), locale)

    @JvmStatic
    @JvmOverloads
    fun formatToString(
        input: Double,
        locale: Locale = getDefaultLocale(),
    ): String {
        val doubleValue = format(input)
        val numberFormat = NumberFormat.getInstance(locale).apply { maximumFractionDigits = 1 }
        return numberFormat.format(doubleValue)
    }

    private fun getDefaultLocale() =
        Locale.getDefault(Locale.Category.FORMAT)
}
