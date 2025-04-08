package com.habitrpg.android.habitica.helpers

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object CrashReporter {
    fun setCustomKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    fun recordException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }
}
