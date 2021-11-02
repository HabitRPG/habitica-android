package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.ui.fragments.preferences.*
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import org.greenrobot.eventbus.Subscribe

class PrefsActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    override fun getLayoutResId(): Int = R.layout.activity_prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(findViewById(R.id.toolbar))

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PreferencesFragment())
            .commit()
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
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

    @Subscribe
    fun showSnackBarEvent(event: ShowSnackbarEvent) {
        HabiticaSnackbar.showSnackbar(findViewById(R.id.snackbar_container), event.leftImage, event.title, event.text, event.specialView, event.rightIcon, event.rightTextColor, event.rightText, event.type)
    }

    private fun createNextPage(preferenceScreen: PreferenceScreen): PreferenceFragmentCompat? =
        when (preferenceScreen.key) {
            "profile" -> ProfilePreferencesFragment()
            "authentication" -> AuthenticationPreferenceFragment()
            "api" -> APIPreferenceFragment()
            "pushNotifications" -> PushNotificationsPreferencesFragment()
            "emailNotifications" -> EmailNotificationsPreferencesFragment()
            else -> null
        }
}
