package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.habitrpg.common.habitica.R as CommonR

object WidgetAuth {
    fun isLoggedIn(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val key = context.getString(CommonR.string.SP_userID)
        return !prefs.getString(key, null).isNullOrEmpty()
    }
}
