package com.habitrpg.android.habitica.extensions

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import java.util.Locale

fun Resources.forceLocale(activity: BaseActivity, locale: Locale) {
    Locale.setDefault(locale)
    val configuration = Configuration()
    configuration.setLocale(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        activity.createConfigurationContext(configuration)
    }
    updateConfiguration(configuration, displayMetrics)
}
