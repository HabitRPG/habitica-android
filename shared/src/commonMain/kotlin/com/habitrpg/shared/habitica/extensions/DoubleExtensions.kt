package com.habitrpg.shared.habitica.extensions

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(decimals: Int): Double = (this * 10.0.pow(decimals.toDouble())).roundToInt() / 10.0.pow(decimals.toDouble())
