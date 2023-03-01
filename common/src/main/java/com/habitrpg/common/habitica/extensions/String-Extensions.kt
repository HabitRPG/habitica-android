package com.habitrpg.common.habitica.extensions

import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.util.Linkify
import java.util.*

fun String.fromHtml(): CharSequence {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}

fun String.addZeroWidthSpace(): CharSequence {
    val spannable: Spannable = SpannableString(this)
    Linkify.addLinks(spannable, Linkify.WEB_URLS)
    // Append a zero-width space to the Spannable to allow clicking
    // on the open spaces (and prevent the link from opening)
    return TextUtils.concat(spannable, "\u200B")
}

fun String.removeZeroWidthSpace(): String {
    return this.replace("\u200B", "")
}

fun String.localizedCapitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun String.spaceBetweenCapitals(): String {
    return this.replace("(.)([A-Z0-9]\\w)".toRegex(), "$1 $2")
}

fun String.localizedCapitalizeWithSpaces(): String {
    return this.localizedCapitalize().spaceBetweenCapitals()
}

