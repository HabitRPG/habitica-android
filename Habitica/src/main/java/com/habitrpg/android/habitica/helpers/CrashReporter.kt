package com.habitrpg.android.habitica.helpers

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import javax.inject.Inject

interface CrashReporter {
    fun setCustomKey(
        key: String,
        value: String,
    )

    fun recordException(throwable: Throwable)
}

class FirebaseCrashReporter
    @Inject
    constructor() : CrashReporter {
        override fun setCustomKey(
            key: String,
            value: String,
        ) {
            Firebase.crashlytics.setCustomKey(key, value)
        }

        override fun recordException(throwable: Throwable) {
            Firebase.crashlytics.recordException(throwable)
        }
    }
