package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.api.HostConfig
import javax.inject.Inject

class APIPreferenceFragment : BasePreferencesFragment() {
    @Inject
    lateinit var hostConfig: HostConfig

    private val apiPreferences: List<String>
        get() = listOf(getString(R.string.SP_APIToken), getString(R.string.SP_userID))

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipMan?.setPrimaryClip(
            if (preference.key == getString(R.string.SP_APIToken)) {
                ClipData.newPlainText(preference.key, hostConfig.apiKey)
            } else {
                ClipData.newPlainText(preference.key, preference.summary)
            }
        )
        Toast.makeText(activity, "Copied " + preference.key + " to clipboard.", Toast.LENGTH_SHORT).show()
        return super.onPreferenceTreeClick(preference)
    }

    override fun setupPreferences() {
        for ((key, value) in preferenceScreen.sharedPreferences.all) {
            if (apiPreferences.contains(key) && value != null) {
                findPreference<Preference>(key)?.summary = value.toString()
            }
        }
    }
}
