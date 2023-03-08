package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.fragments.preferences.AccountPreferenceFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.EmailNotificationsPreferencesFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.PushNotificationsPreferencesFragment
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrefsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback, SnackbarActivity {

    override fun getLayoutResId(): Int = R.layout.activity_prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(findViewById(R.id.toolbar))

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PreferencesFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartScreen(
        preferenceFragment: PreferenceFragmentCompat,
        preferenceScreen: PreferenceScreen
    ): Boolean {
        val fragment = createNextPage(preferenceScreen)
        if (fragment != null) {
            val arguments = Bundle()
            arguments.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.key)
            fragment.arguments = arguments
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            return true
        }
        return false
    }

    private fun createNextPage(preferenceScreen: PreferenceScreen): PreferenceFragmentCompat? =
        when (preferenceScreen.key) {
            "my_account" -> AccountPreferenceFragment()
            "pushNotifications" -> PushNotificationsPreferencesFragment()
            "emailNotifications" -> EmailNotificationsPreferencesFragment()
            else -> null
        }

    override fun snackbarContainer(): ViewGroup {
        val v = findViewById<ViewGroup>(R.id.snackbar_container)
        return v
    }
}
