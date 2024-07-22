package com.habitrpg.android.habitica.extensions

import com.habitrpg.common.habitica.helpers.LanguageHelper
import java.util.Locale



fun Locale.getSystemDefault(): Locale {
    return LanguageHelper.systemLocale
}
