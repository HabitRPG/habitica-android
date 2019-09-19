package com.habitrpg.android.habitica.helpers

import java.util.*

class LanguageHelper(languageSharedPref: String?) {

    var locale: Locale? = null
        private set
    var languageCode: String? = null
        private set

    init {
        when (languageSharedPref) {
            "iw" -> {
                locale = Locale("iw")
                languageCode = "he"
            }
            "hr" -> {
                locale = Locale("hr", "HR")
                languageCode = "hr"
            }
            "in" -> {
                locale = Locale("in")
                languageCode = "id"
            }
            "pt" -> {
                locale = Locale("pt", "PT")
                languageCode = "pt"
            }
            else -> {
                languageSharedPref?.let { pref ->
                    locale = if (pref.contains("_")) {
                        val languageCodeParts = pref.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        Locale(languageCodeParts[0], languageCodeParts[1])
                    } else {
                        Locale(languageSharedPref)
                    }
                }
                languageCode = languageSharedPref
            }
        }
    }
}
