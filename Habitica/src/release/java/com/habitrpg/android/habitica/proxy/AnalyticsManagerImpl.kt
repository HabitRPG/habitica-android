package com.habitrpg.android.habitica.proxy

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.habitrpg.common.habitica.helpers.AnalyticsManager

class AnalyticsManagerImpl(context: Context) : AnalyticsManager {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

}
