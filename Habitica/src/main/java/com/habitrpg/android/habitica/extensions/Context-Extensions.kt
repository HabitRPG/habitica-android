package com.habitrpg.android.habitica.extensions

import android.app.Service
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.LayoutInflater

val Context.layoutInflater: LayoutInflater
    get() = this.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater


fun Context.getThemeColor(colorRes: Int): Int {
    val value = TypedValue()
    theme.resolveAttribute(colorRes, value, true)
    return value.data
}

fun Context.isUsingNightModeResources(): Boolean {
    return when (resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> true
        Configuration.UI_MODE_NIGHT_NO -> false
        Configuration.UI_MODE_NIGHT_UNDEFINED -> false
        else -> false
    }
}