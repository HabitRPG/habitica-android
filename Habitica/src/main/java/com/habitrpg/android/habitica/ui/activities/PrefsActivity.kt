package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceScreen
import android.support.v7.widget.Toolbar

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.ui.fragments.preferences.PreferencesFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.PushNotificationsPreferencesFragment

import butterknife.BindView
import com.habitrpg.android.habitica.ui.fragments.preferences.APIPreferenceFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.AuthenticationPreferenceFragment
import com.habitrpg.android.habitica.ui.fragments.preferences.ProfilePreferencesFragment

class PrefsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun getLayoutResId(): Int = R.layout.activity_prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, PreferencesFragment())
                .commit()
    }

    override fun injectActivity(component: AppComponent) {
        component.inject(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartScreen(preferenceFragment: PreferenceFragmentCompat,
                                         preferenceScreen: PreferenceScreen): Boolean {
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
                "profile" -> ProfilePreferencesFragment()
                "authentication" -> AuthenticationPreferenceFragment()
                "api" -> APIPreferenceFragment()
                "pushNotifications" -> PushNotificationsPreferencesFragment()
                else -> null
            }
}

