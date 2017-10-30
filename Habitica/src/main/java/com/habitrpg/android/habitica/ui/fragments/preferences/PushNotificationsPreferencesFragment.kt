package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import com.habitrpg.android.habitica.HabiticaBaseApplication

class PushNotificationsPreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.getComponent().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun setupPreferences() {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

    }
}