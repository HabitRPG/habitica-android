package com.habitrpg.android.habitica.extensions

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.Animal

fun Animal.getTranslatedType(c: Context?): String {
    if (c == null) {
        return type
    }

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