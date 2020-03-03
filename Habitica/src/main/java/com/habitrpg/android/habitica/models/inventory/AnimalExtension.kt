package com.habitrpg.android.habitica.models.inventory

import android.content.Context
import com.habitrpg.android.habitica.R

fun Animal.getTranslatedType(c: Context?): String {
    var currType = type

    if (currType == "drop") {
        currType = c?.getString(R.string.standard)
    }
    if (currType == "quest") {
        currType = c?.getString(R.string.quest)
    }
    if (currType == "wacky") {
        currType = c?.getString(R.string.wacky)
    }
    if (currType == "special") {
        currType = c?.getString(R.string.special)
    }

    return currType
}