package com.habitrpg.common.habitica.helpers

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

class LanguageHelper(languageSharedPref: String?) {
    var locale: Locale
        private set
    var languageCode: String? = null
        private set
    var localeListCompat: LocaleListCompat
        private set

    init {
        when (val pref = languageSharedPref ?: "en") {
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
                locale =
                    if (pref.contains("_")) {
                        val languageCodeParts = pref.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        Locale(languageCodeParts[0], languageCodeParts[1])
                    } else {
                        Locale(pref)
                    }
                languageCode = languageSharedPref
            }
        }
        localeListCompat = LocaleListCompat.create(locale)
    }

    companion object {
        // Intentional, we want the system locale, not the app locale
        @SuppressLint("ConstantLocale")
        val systemLocale: Locale

        init {
            systemLocale = Locale.getDefault()
        }
        
        fun getLanguageTag(languagePref: String?): String {
            return when (languagePref) {
                "iw" -> "iw"
                "hr" -> "hr-HR"
                "in" -> "in"
                "pt" -> "pt-PT"
                "pt_BR" -> "pt-BR"
                "en_GB" -> "en-GB"
                "zh_TW" -> "zh-TW"
                else -> languagePref?.replace("_", "-") ?: "en"
            }
        }
    }
}
