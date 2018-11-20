package com.habitrpg.android.habitica.helpers

object HealthFormatter {
    fun format(input: Int) = format(input.toDouble())

    fun format(input: Double) =
            if (input < 1 && input > 0) {
                Math.ceil(input * 10) / 10
            } else {
                Math.floor(input)
            }
}