package com.habitrpg.android.habitica.helpers

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

object CrashReporter {
    fun setCustomKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    fun recordException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }
}
