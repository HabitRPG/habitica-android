package com.habitrpg.android.habitica.extensions

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import java.util.Locale

fun Resources.forceLocale(
    activity: BaseActivity,
    locale: Locale
) {
    Locale.setDefault(locale)
    val configuration = Configuration()
    configuration.setLocale(locale)
    activity.createConfigurationContext(configuration)
    updateConfiguration(configuration, displayMetrics)

    try {
        Firebase.crashlytics.setCustomKey("language", locale.toLanguageTag())
    } catch (_: IllegalStateException) {
        // issue with getting firebase
    }
}
