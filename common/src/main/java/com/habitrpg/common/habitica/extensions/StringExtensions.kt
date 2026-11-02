package com.habitrpg.common.habitica.extensions

import android.text.Html
import android.util.Patterns
import java.util.Locale

fun String.fromHtml(): CharSequence = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)

fun String.localizedCapitalize(): String =
    this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

fun String.spaceBetweenCapitals(): String = this.replace("(.)([A-Z0-9]\\w)".toRegex(), "$1 $2")

fun String.localizedCapitalizeWithSpaces(): String = this.localizedCapitalize().spaceBetweenCapitals()

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.toLocale(): Locale {
    val parts = this.split("_")
    return when (parts.size) {
        1 -> Locale(parts[0])
        2 -> Locale(parts[0], parts[1])
        else -> Locale("en")
    }
}
