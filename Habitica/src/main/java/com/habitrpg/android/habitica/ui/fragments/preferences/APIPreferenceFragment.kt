package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import java.util.*

class APIPreferenceFragment: BasePreferencesFragment() {
    private val apiPreferences: List<String>
        get() = Arrays.asList(getString(R.string.SP_APIToken), getString(R.string.SP_userID))


    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.component?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipMan?.primaryClip = ClipData.newPlainText(preference.key, preference.summary)
        Toast.makeText(activity, "Copied " + preference.key + " to clipboard.", Toast.LENGTH_SHORT).show()
        return super.onPreferenceTreeClick(preference)
    }

    override fun setupPreferences() {
        for ((key, value) in preferenceScreen.sharedPreferences.all) {
            if (apiPreferences.contains(key) && value != null) {
                findPreference(key).summary = value.toString()
            }
        }
    }

}