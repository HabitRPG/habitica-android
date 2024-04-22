package com.habitrpg.android.habitica.extensions

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.Animal

fun Animal.getTranslatedType(c: Context?): String? {
    return getTranslatedAnimalType(c, type)
}

fun getTranslatedAnimalType(
    c: Context?,
    type: String?,
): String? {
    if (c == null) {
        return type
    }

    return when (type) {
        "drop" -> c.getString(R.string.standard)
        "quest" -> c.getString(R.string.quest)
        "wacky" -> c.getString(R.string.wacky)
        "special" -> c.getString(R.string.special)
        "premium" -> c.getString(R.string.magic_potion)
        else -> {
            type
        }
    }
}
