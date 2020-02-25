package com.habitrpg.shared.habitica.models.user

import android.content.Context
import space.thelen.shared.cluetective.R

actual fun nativeGetTranslatedClassName(context: Context, habitClass: String?): String {
    return when (habitClass) {
        Stats.HEALER -> context.getString(R.string.healer)
        Stats.ROGUE -> context.getString(R.string.rogue)
        Stats.WARRIOR -> context.getString(R.string.warrior)
        Stats.MAGE -> context.getString(R.string.mage)
        else -> context.getString(R.string.warrior)
    }
}
