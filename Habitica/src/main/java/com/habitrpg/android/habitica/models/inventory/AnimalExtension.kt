package com.habitrpg.android.habitica.models.inventory

import android.content.Context
import com.habitrpg.android.habitica.R

fun Animal.getTranslatedType(c: Context?): String {

    var currType: String = when (type) {
        "drop"    -> c?.getString(R.string.standard).toString()
        "quest"   -> c?.getString(R.string.quest).toString()
        "wacky"   -> c?.getString(R.string.wacky).toString()
        "special" -> c?.getString(R.string.special).toString()
        else      -> {
            type
        }
    }

    return currType
}